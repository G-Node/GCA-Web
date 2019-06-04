import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class LoginTest(unittest.TestCase):
    @classmethod
    def setUpClass(inst):
        inst.driver = webdriver.Chrome()
        inst.driver.get("http://localhost:9000/login")
        inst.driver.maximize_window()

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
        driver.find_element_by_id("usermenu").click()
        driver.find_element_by_partial_link_text("Logout").click()
        self.assertTrue(driver.find_element_by_partial_link_text("Login"))

    @classmethod
    def tearDownClass(inst):
        inst.driver.close()


if __name__ == "__main__":
    unittest.main()
