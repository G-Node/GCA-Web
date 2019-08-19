import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
import Cookies


@pytest.mark.usefixtures("setup_login")
class TestLogin:

    def test_login(self, user="alice@foo.com", password="testtest"):
        driver = self.driver
        driver.find_element_by_id("identifier").send_keys("wrong_alice")
        driver.find_element_by_id("password").send_keys(password)
        driver.find_element_by_id("submit").click()
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, "password"))
        )

        driver.find_element_by_id("identifier").send_keys(user)
        driver.find_element_by_id("password").send_keys("wrong_password")
        driver.find_element_by_id("submit").click()
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, "password"))
        )

        driver.find_element_by_id("identifier").send_keys(user)
        driver.find_element_by_id("password").send_keys(password)
        driver.find_element_by_id("submit").click()
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, "usermenu"))
        )

    def test_logout(self):
        driver = self.driver
        Cookies.load_cookies(driver)
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conferences")
        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="usermenu"]'))
        )
        driver.find_element_by_id("usermenu").click()
        driver.find_element_by_partial_link_text("Logout").click()
        assert driver.find_element_by_partial_link_text("Login")
