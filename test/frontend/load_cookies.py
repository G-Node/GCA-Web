from selenium import webdriver
import Cookies

driver = webdriver.Chrome()
Cookies.set_cookies(driver, "alice@foo.com", "testtest")
driver.quit()
