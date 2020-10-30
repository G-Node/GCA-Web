import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
import Cookies


@pytest.mark.usefixtures("setup_login")
class TestLogin:

    def wait_until(self, delay, condition):
        WebDriverWait(self.driver, delay).until(condition)

    def test_login(self, user="alice@foo.com", password="testtest"):
        driver = self.driver

        # Test invalid username
        driver.find_element_by_id("identifier").send_keys("wrong_alice")
        driver.find_element_by_id("password").send_keys(password)
        driver.find_element_by_id("submit").click()

        # Presence of the 'password' element implies a failed login
        self.wait_until(30, EC.presence_of_element_located((By.ID, "password")))

        # Test invalid password
        driver.find_element_by_id("identifier").send_keys(user)
        driver.find_element_by_id("password").send_keys("wrong_password")
        driver.find_element_by_id("submit").click()

        self.wait_until(30, EC.presence_of_element_located((By.ID, "password")))

        # Test valid login
        driver.find_element_by_id("identifier").send_keys(user)
        driver.find_element_by_id("password").send_keys(password)
        driver.find_element_by_id("submit").click()

        # Presence of the 'usermenu' element implies successful login
        self.wait_until(30, EC.presence_of_element_located((By.ID, "usermenu")))

    def test_logout(self):
        driver = self.driver

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conferences")
        self.wait_until(30, EC.visibility_of_element_located((By.XPATH, '//*[@id="usermenu"]')))

        driver.find_element_by_id("usermenu").click()
        driver.find_element_by_partial_link_text("Logout").click()
        assert driver.find_element_by_partial_link_text("Login")
