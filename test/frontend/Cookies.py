import pickle

from selenium import webdriver


def save_cookies(driver, location):

    pickle.dump(driver.get_cookies(), open(location, "wb"))


def load_cookies(driver, location, url=None):

    cookies = pickle.load(open(location, "rb"))
    driver.delete_all_cookies()
    # have to be on a page before you can add any cookies, any page - does not matter which
    driver.get("http://localhost:9000/login" if url is None else url)
    for cookie in cookies:
        driver.add_cookie(cookie)


def delete_cookies(driver, domains=None):

    if domains is not None:
        cookies = driver.get_cookies()
        original_len = len(cookies)
        for cookie in cookies:
            if str(cookie["domain"]) in domains:
                cookies.remove(cookie)
        if len(cookies) < original_len:  # if cookies changed, we will update them
            # deleting everything and adding the modified cookie object
            driver.delete_all_cookies()
            for cookie in cookies:
                driver.add_cookie(cookie)
    else:
        driver.delete_all_cookies()


def set_cookies(driver, user, password):

    cookies_location = "cookies.txt"
    driver.get("http://localhost:9000/login")
    driver.maximize_window()
    driver.find_element_by_id("identifier").send_keys(user)
    driver.find_element_by_id("password").send_keys(password)
    driver.find_element_by_id("submit").click()
    save_cookies(driver, cookies_location)
