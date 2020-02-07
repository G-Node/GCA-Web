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