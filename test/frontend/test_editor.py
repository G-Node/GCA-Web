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
        driver.find_element_by_id('button-action').click()
        assert EC.visibility_of((By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/h4'))

    def test_title(self):
        driver = self.driver
        self.click_edit_button('title')

        #wait for dialog to open
        #xpath neccessary, as several copies in source code
        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="title-editor"]'))
        )

        #get title and set keys
        title = driver.find_element_by_xpath('//*[@id="title-editor"]//input[@id="title"]')
        title.send_keys('New Test Abstract')

        #close modal
        driver.find_element(By.XPATH, '//*[@id="title-editor"]//button[@id="modal-button-ok"]').click()

        #save
        WebDriverWait(driver, 30).until(
            EC.element_to_be_clickable((By.ID, 'button-action'))
        )
        driver.find_element_by_id('button-action').click()

        #make sure, issues are shown
        WebDriverWait(driver, 30).until(
            EC.text_to_be_present_in_element((By.ID, 'lblvalid'), 'issues')
        )

        #make sure, submit fails
        driver.find_element_by_id('button-action').click()
        assert EC.text_to_be_present_in_element(
            (By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/p'), 'Unable to submit'
        )