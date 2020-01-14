import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.action_chains import ActionChains
from conftest import move_to_element_by_class_name



@pytest.mark.usefixtures("setup_editor")
class TestEditor:

    def click_edit_button(self, name):
        driver = self.driver
        if 'firefox' in driver.capabilities['browserName'] and name != 'title':
            move_to_element_by_class_name(driver, 'title')
        move_to_element_by_class_name(driver, name)
        WebDriverWait(driver, 10).until(
            EC.visibility_of_element_located((By.ID, 'button-edit-' + name))
        )
        driver.find_element_by_id('button-edit-' + name).click()

    def test_simple_creation(self):
        driver = self.driver
        #fail saving when empty abstract
        driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div[2]/div[1]/span/button').click()
        assert EC.visibility_of((By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/h4'))

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
        assert EC.text_to_be_present_in_element(
            (By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/p'), 'Unable to submit'
        )
