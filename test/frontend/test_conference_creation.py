import pytest
import Cookies
from uuid import uuid1
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from conftest import *


@pytest.mark.usefixtures("setup_conference_creation")
class TestConferenceCreation:

    def test_name(self):
        driver = self.driver
        element_send_keys_by_id(driver, 'name', "Test Conference")
        element_click_by_class_name(driver, 'btn-success')

        #test automatic fill-in
        assert "TC" in element_get_attribute_by_id(driver, 'short', 'value')
        assert "500" == element_get_attribute_by_id(driver, 'mAbsLen', 'value')
        assert "0" == element_get_attribute_by_id(driver, 'mFigs', 'value')

    def test_short(self):
        driver = self.driver
        tc_num = "TC" + str(uuid1())[0:8]
        element_send_keys_by_id(driver, 'short', 'BC14')

        element_click_by_class_name(driver, 'btn-success')

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '//div[contains(@class,"alert-danger")]/strong'))
        )
        assert "Conference short is already in use." in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        element_send_keys_by_id(driver, 'short', tc_num)

        element_click_by_class_name(driver, 'btn-success')

        WebDriverWait(driver, 30).until(
            EC.invisibility_of_element_located((By.XPATH, '//div[contains(@class,"alert-danger")]/strong'))
        )
        assert len(driver.find_elements_by_xpath('//div[contains(@class,"alert-danger")]/strong')) == 0

    def test_published(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'published'))
        )
        element_click_by_id(driver, 'published')

    # thumbnail only shown if conference not active
    def test_thumbnail_url(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'thumbnail'))
        )
        element_send_keys_by_id(driver, 'thumbnail', 'https://portal.g-node.org/abstracts/bc14/BC14_icon.png')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

    def test_active(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'active'))
        )
        element_click_by_id(driver, 'active')

    def test_submission(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'submission'))
        )
        element_click_by_id(driver, 'submission')

        element_click_by_class_name(driver, 'btn-success')
