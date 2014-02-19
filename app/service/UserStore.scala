package service

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import securesocial.core.SocialUser
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import org.joda.time.DateTime
import utils.AnormExtension._

class UserStore(application: Application) extends UserServicePlugin(application) {

  private val userParser :RowParser[SocialUser] = {
    get[String]("authMethod") ~ get[String]("id") ~ get[String]("provider") ~ get[String]("firstName") ~
      get[String]("lastName") ~ get[String]("fullName") ~ get[Option[String]]("email") ~
      get[Option[String]]("avatar") ~  get[Option[String]]("oa1Token") ~
      get[Option[String]]("oa1Secret") ~ get[Option[String]]("oa2Token") ~ get[Option[String]]("oa2Type") ~
      get[Option[Int]]("oa2ExpiresIn") ~ get[Option[String]]("oa2RefreshToken") ~
      get[Option[String]]("pwHasher") ~ get[Option[String]]("pwPassword") ~ get[Option[String]]("pwSalt") map {
      case "userPassword" ~ i ~ p ~ f ~ l ~ fn ~ e ~ a ~ _ ~ _ ~ _ ~ _  ~ _ ~ _ ~ h ~ pw ~ s =>
        new SocialUser(IdentityId(i, p), f, l, fn, e, a, AuthenticationMethod.UserPassword, None, None, Some(new PasswordInfo(h.get, pw.get, s)))
      case "oauth2" ~ i ~ p ~ f ~ l ~ fn ~ e ~ a ~ _ ~ _ ~ to ~ ty  ~ ex ~ re ~ _ ~ _ ~ _ =>
        new SocialUser(IdentityId(i, p), f, l, fn, e, a, AuthenticationMethod.OAuth2, None, Some(new OAuth2Info(to.get, ty, ex, re)), None)
      case "oauth1" ~ i ~ p ~ f ~ l ~ fn ~ e ~ a ~ to  ~ ts ~ _ ~ _  ~ _ ~ _ ~ _ ~ _ ~ _ =>
        new SocialUser(IdentityId(i, p), f, l, fn, e, a, AuthenticationMethod.OAuth1, Some(new OAuth1Info(to.get, ts.get)), None, None)
    }
  }

  def find(id: IdentityId): Option[Identity] = {
    Logger.debug("find")

    DB.withConnection{ implicit c =>
      SQL("SELECT * from Users u WHERE lower(u.id)=lower({id}) AND u.provider={provider}").onParams(id.userId,
        id.providerId).as(userParser.singleOpt)
    }

  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    Logger.debug("findByEmailAndProvider $email, $providerId")

    DB.withConnection{ implicit c =>
      SQL("SELECT * from Users u WHERE lower(u.email)=lower({mail}) AND u.provider={provider}").onParams(email,
        providerId).as(userParser.singleOpt)
    }
  }


  def save(user: Identity): Identity = {

    val newUser = this.find(user.identityId) match {
      case Some(u) => false
      case None => true
    }

    if(!newUser) {
      Logger.info("Have user already in the db!")
    }

    //FIXME: check if user is different and if not don't do anything

    DB.withConnection { implicit c =>

      if (newUser) {

        SQL("""INSERT INTO Users (id, provider, firstName, lastName, fullName, email, avatar, authMethod) VALUES
              | ({id}, {provider}, {firstName}, {lastName}, {fullName}, {email}, {avatar}, {authMethod})
            """.stripMargin).onParams(user.identityId.userId, user.identityId.providerId, user.firstName, user.lastName,
            user.fullName, user.email, user.avatarUrl, user.authMethod.method).execute()

      } else {

        SQL("""UPDATE Users u SET firstName={firstName}, lastName={lastName},
              | fullName={fullName}, email={email}, avatar={avatar}, authMethod={authMethod}
              | WHERE u.id={id} AND u.provider={provider}
            """.stripMargin).onParams(user.firstName, user.lastName, user.fullName, user.email,
            user.avatarUrl, user.authMethod.method, user.identityId.userId, user.identityId.providerId).execute()
      }

      user.oAuth1Info.map { oa => SQL(
        """UPDATE Users u SET oa1Token={token}, oa1Secret={secret}
          | WHERE u.id={id} AND u.provider={provider}
        """.stripMargin).onParams(oa.token, oa.secret, user.identityId.userId, user.identityId.providerId).execute()
      }

      user.oAuth2Info.map { oa => SQL(
        """UPDATE Users u SET oa2Token={token}, oa2Type={type}, oa2ExpiresIn={expires}, oa2RefreshToken={refresh}
          | WHERE u.id={id} AND u.provider={provider}
        """.stripMargin).onParams(oa.accessToken, oa.tokenType, oa.expiresIn, oa.refreshToken,
          user.identityId.userId, user.identityId.providerId).execute()
      }

      user.passwordInfo.map{ info => SQL(
        """UPDATE Users u SET pwHasher={haser}, pwPassword={pw}, pwSalt={salt}
          | WHERE u.id={id} AND u.provider={provider}""".stripMargin).onParams(
          info.hasher, info.password, info.salt, user.identityId.userId, user.identityId.providerId).execute()
      }
    }

    user
  }

  private val tokenParser : anorm.RowParser[Token] = {
      get[String]("id") ~
      get[String]("email") ~
      get[DateTime]("creationTime") ~
      get[DateTime]("expirationTime") ~
      get[Boolean]("isSignUp") map {
      case i ~ e ~ ctime ~ etime ~ s => Token(i, e, ctime, etime, s)
    }
  }

  def save(token: Token) {

    DB.withConnection { implicit c =>
      SQL("""insert into Tokens(id, email, creationTime, expirationTime, isSignUp)
            |values({id}, {email}, {creationTime}, {expirationTime}, {isSignUp})
          """.stripMargin).onParams(token.uuid, token.email, token.creationTime,
                                    token.expirationTime, token.isSignUp).execute()
    }
  }

  def findToken(token: String): Option[Token] = {
    DB.withConnection{ implicit c =>
      SQL("select * from Tokens t where t.id = {uuid}").onParams(token).as(tokenParser.singleOpt)
    }
  }

  def deleteToken(uuid: String) {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens t where t.id = {uuid}").onParams(uuid).execute()
    }
  }

  def deleteTokens() {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens").execute()
    }
  }

  def deleteExpiredTokens() {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens t where t.expirationTime > now()").execute()
    }
  }
}