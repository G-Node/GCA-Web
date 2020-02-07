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

        #move to front page and check, whether conference is shown and marked as "Unpublished"
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        assert len(driver.find_elements_by_xpath('//div[@class="media-body"]/h4[@id="' + tc_num + '"]')) == 1

        move_to_element_by_xpath(driver, '//div[@class="media-body"]/h4[@id="' + tc_num + '"]')
        conf_div = driver.find_element_by_xpath('//div[@class="media-body"]/h4[@id="' + tc_num + '"]/..')
        assert len(conf_div.find_elements_by_xpath('./h4[contains(text(),"Unpublished")]')) == 1
        assert len(conf_div.find_elements_by_xpath('./h4[contains(text(),"Test Conference")]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()


    def test_published(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'published'))
        )
        element_click_by_id(driver, 'published')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        assert len(driver.find_elements_by_xpath('//div[@class="media-body"]/a[contains(@href,"' + tc_num + '")]')) == 1

        conf_div = driver.find_element_by_xpath('//div[@class="media-body"]/a[contains(@href,"' + tc_num + '")]/..')
        assert len(conf_div.find_elements_by_xpath('./h4')) == 0
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Manage")]')) == 1
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Conference Settings")]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    # thumbnail only shown if conference not active
    def test_thumbnail_url(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'thumbnail'))
        )
        element_send_keys_by_id(driver, 'thumbnail', 'https://portal.g-node.org/abstracts/bc14/BC14_icon.png')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        conf_div = driver.find_element_by_xpath('//div[@class="media-body"]/a[contains(@href,"' + tc_num + '")]/..')

        assert len(conf_div.find_elements_by_xpath('..//div[contains(@class,"media-left")]//a/img[@src='
                                                   '"https://portal.g-node.org/abstracts/bc14/BC14_icon.png"]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    def test_active(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'active'))
        )
        element_click_by_id(driver, 'active')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        assert len(driver.find_elements_by_xpath('//div[@class="jumbotron"]/h3'
                                                 '/a[contains(@href,"' + tc_num + '")]')) == 1

        move_to_element_by_xpath(driver, '//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]')
        conf_div = driver.find_element_by_xpath('//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Manage")]')) == 1
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Conference Settings")]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    def test_submission(self):
        driver = self.driver

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'short'))
        )
        tc_num = element_get_attribute_by_id(driver, 'short', 'value')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)
        assert len(driver.find_elements_by_xpath('//ul[contains(@class,"nav")]'
                                                 '//li/a[contains(text(),"Submission")]')) == 0
        assert len(driver.find_elements_by_xpath('//div[@class="jumbotron"]//a[contains(text(),"Submit")]')) == 0

        element_click_by_xpath(driver, '//div[@class="jumbotron"]//a[contains(text(),"Conference Settings")]')

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'submission'))
        )
        element_click_by_id(driver, 'submission')

        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        assert len(driver.find_elements_by_xpath('//ul[contains(@class,"nav")]'
                                                 '//li/a[contains(text(),"Submission")]')) == 1
        assert len(driver.find_elements_by_xpath('//div[@class="jumbotron"]//a[contains(text(),"Submit")]')) == 1

        element_click_by_xpath(driver, '//div[@class="jumbotron"]//a[contains(text(),"Conference Settings")]')

    def test_group(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'group'))
        )
        element_send_keys_by_id(driver, 'group', 'Test Group 1')

    def test_cite(self):
        driver = self.driver
        element_send_keys_by_id(driver, 'cite', 'The Test Conf, Somewhere, Sometime')

        element_click_by_class_name(driver, 'btn-success')

    def test_start_date(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'start'))
        )
        element_click_by_id(driver, 'start')

        move_to_element_by_id(driver, 'ui-datepicker-div')
        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "14")]').click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        element_click_by_class_name(driver, 'btn-success')

        # other elements might not be reachable immediately after click of "Save" button
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'start'))
        )
        assert "14" in element_get_attribute_by_xpath(driver, '//*[@id="start"]', 'value')

        old_date = element_get_attribute_by_id(driver, 'start', 'value')
        element_send_keys_by_id(driver, 'start', '99/99/9999')

        element_click_by_id(driver, 'mFigs')

        move_to_element_by_id(driver, 'start')
        assert old_date in element_get_attribute_by_id(driver, 'start', 'value')

        driver.find_element_by_id('start').send_keys('hello')
        assert old_date in element_get_attribute_by_id(driver, 'start', 'value')

        element_click_by_id(driver, 'mFigs')
        tc_num = element_get_attribute_by_id(driver, 'short', 'value')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        move_to_element_by_xpath(driver, '//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        conf_div = driver.find_element_by_xpath('//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        assert len(conf_div.find_elements_by_xpath('./p[contains(text(),"14")]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    def test_end_date(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'end'))
        )
        element_click_by_id(driver, 'end')

        move_to_element_by_id(driver, 'ui-datepicker-div')
        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "16")]').click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        element_click_by_class_name(driver, 'btn-success')

        # other elements might not be reachable immediately after click of "Save" button
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'end'))
        )
        assert "16" in element_get_attribute_by_xpath(driver, '//*[@id="end"]', 'value')

        old_date = element_get_attribute_by_id(driver, 'end', 'value')
        element_send_keys_by_id(driver, 'end', '99/99/9999')

        element_click_by_id(driver, 'mFigs')

        move_to_element_by_id(driver, 'end')
        assert old_date in element_get_attribute_by_id(driver, 'end', 'value')

        driver.find_element_by_id('end').send_keys('hello')
        assert old_date in element_get_attribute_by_id(driver, 'end', 'value')

        element_click_by_id(driver, 'mFigs')
        tc_num = element_get_attribute_by_id(driver, 'short', 'value')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        move_to_element_by_xpath(driver, '//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        conf_div = driver.find_element_by_xpath('//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        assert len(conf_div.find_elements_by_xpath('./p[contains(text(),"16")]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    def test_deadline(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'deadline'))
        )
        element_click_by_id(driver, 'deadline')
        driver.find_element_by_id('deadline').click()

        move_to_element_by_id(driver, 'ui-datepicker-div')
        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "10")]').click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        element_click_by_class_name(driver, 'btn-success')

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'deadline'))
        )
        assert "10" in element_get_attribute_by_xpath(driver, '//*[@id="deadline"]', 'value')

        old_date = element_get_attribute_by_id(driver, 'deadline', 'value')
        element_send_keys_by_id(driver, 'deadline', '99/99/9999')
        element_click_by_id(driver, "mFigs")

        assert old_date in element_get_attribute_by_id(driver, 'deadline', 'value')
        driver.find_element_by_id('deadline').send_keys('hello')
        element_click_by_id(driver, "mFigs")
        assert old_date in element_get_attribute_by_id(driver, 'deadline', 'value')
        element_click_by_id(driver, "mFigs")

    def test_logo_url(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'logo'))
        )
        element_send_keys_by_id(driver, 'logo', 'https://portal.g-node.org/abstracts/bc18/BC18_header.jpg')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        move_to_element_by_xpath(driver, '//div[@class="jumbotron"]/h3/a[contains(@href,"' + tc_num + '")]/../..')
        conf_div = driver.find_element_by_xpath('//div[@class="jumbotron"]/h3'
                                                '/a[contains(@href,"' + tc_num + '")]/../..')
        assert len(conf_div.find_elements_by_xpath('.//p[contains(@data-bind,"logo")]//img[@src='
                                                   '"https://portal.g-node.org/abstracts/bc18/BC18_header.jpg"]')) == 1

        conf_div.find_element_by_xpath('.//a[contains(text(),"Conference Settings")]').click()

    def test_iosapp(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'iosapp'))
        )
        element_send_keys_by_id(driver, 'iosapp', '999999999')

    def test_link(self):
        driver = self.driver
        element_send_keys_by_id(driver, 'link', 'http://www.nncn.de/en/bernstein-conference/2014')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        assert len(driver.find_elements_by_xpath(
            '//div[@class="jumbotron"]/div[contains(@data-bind,"logo")]'
            '/a[contains(@href,"http://www.nncn.de/en/bernstein-conference/2014")]')) == 1

        element_click_by_xpath(driver, '//div[@class="jumbotron"]//a[contains(text(),"Conference Settings")]')

    def test_description(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'desc'))
        )
        element_send_keys_by_id(driver, 'desc', 'Important conference.')

    def test_notice(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'notice'))
        )
        element_send_keys_by_id(driver, 'notice', 'Check abstracts before submission!')

    def test_presentation_preferences(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'presentation'))
        )
        element_click_by_id(driver, 'presentation')

    def test_add_topic(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'addTopic'))
        )
        element_send_keys_by_id(driver, 'addTopic', 'Topic 1')
        driver.find_element_by_id('btn-add-topic').click()

        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 1")]')) == 1

        element_send_keys_by_id(driver, 'addTopic', 'Topic 2')
        driver.find_element_by_id('btn-add-topic').click()

        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 2")]')) == 1

    def test_remove_topic(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'addTopic'))
        )

        move_to_element_by_xpath(driver, '//ul/li/span[contains(text(),"Topic 1")]/../a')
        driver.find_element_by_xpath('//ul/li/span[contains(text(),"Topic 1")]/../a').click()

        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 1")]')) == 0
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 2")]')) == 1

    def test_maximum_abstract_length(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'mAbsLen'))
        )
        # initial mAbsLen = 2000
        element_send_keys_by_id(driver, 'mAbsLen', '300')

        assert "Changing to shorter abstract length causes cut-offs" in \
               driver.find_element_by_xpath('//div[contains(@class,"form-group")]'
                                            '/div[contains(@class,"alert-danger")]').text

        element_click_by_class_name(driver, 'btn-success')

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'mAbsLen'))
        )
        move_to_element_by_id(driver, 'mAbsLen')
        assert "300" == driver.find_element_by_id('mAbsLen').get_attribute('value')

    def test_maximum_figures(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'mFigs'))
        )
        element_send_keys_by_id(driver, 'mFigs', '3')

        element_click_by_class_name(driver, 'btn-success')

        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'mFigs'))
        )
        move_to_element_by_id(driver, 'mFigs')
        assert "3" == driver.find_element_by_id('mFigs').get_attribute('value')
