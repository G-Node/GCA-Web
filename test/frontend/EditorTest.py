import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.action_chains import ActionChains
import Cookies

class EditorTest(unittest.TestCase):
    @classmethod
    def setUpClass(inst):
        inst.driver = webdriver.Chrome()
        #browser needs to be opened before cookies can be loaded
        inst.driver.get("http://localhost:9000/login")
        Cookies.load_cookies(inst.driver, "cookies.txt")
        inst.driver.maximize_window()
        inst.driver.get("http://localhost:9000/conference/BC14/submission")
        WebDriverWait(inst.driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button'))
        )

    def test_simple_creation(self):
        driver = self.driver
        #fail saving when empty abstract
        driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button').click()
        self.assertTrue(EC.visibility_of((By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/h4')))

    def test_title_save(self):
        driver = self.driver
        form_title = driver.find_element_by_xpath('/html/body/div[2]/div[2]/div[4]/div[1]/div[3]/h2')
        hover = ActionChains(driver).move_to_element(form_title)
        hover.perform()
        driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div[4]/div[1]/div[1]/button').click()

        #wait for dialog to open
        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="title-editor"]'))
        )
        title = driver.find_element_by_xpath('//*[@id="title-editor"]/div/div/div[2]/input')
        title.send_keys('New Test Abstract')
        driver.find_element(By.XPATH, '//*[@id="title-editor"]/div/div/div[3]/button[2]').click()

        #save
        WebDriverWait(driver, 30).until(
            EC.element_to_be_clickable((By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button'))
        )
        driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button').click()
        #make sure, issues are shown
        WebDriverWait(driver, 30).until(
            EC.text_to_be_present_in_element((By.XPATH, '//*[@id="lblvalid"]'), 'issues')
        )

        #make sure, submit fails
        driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button').click()
        self.assertTrue(EC.text_to_be_present_in_element(
            (By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/p'), 'Unable to submit'
        ))

    @classmethod
    def tearDownClass(inst):
        inst.driver.close()


if __name__ == "__main__":
    unittest.main()
