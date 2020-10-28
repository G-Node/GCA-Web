import pytest

from uuid import uuid1

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

import conftest
import Cookies

@pytest.mark.usefixtures("setup_conference_creation")
class TestConferenceCreation:

    def wait_until(self, delay, condition):
        WebDriverWait(self.driver, delay).until(condition)

    def test_name(self):
        driver = self.driver

        # Save a new conference with conference name only
        conftest.element_send_keys_by_id(driver, 'name', "Test Conference")
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Test automatically filled in values
        assert "TC" in conftest.element_get_attribute_by_id(driver, 'short', 'value')
        assert "500" == conftest.element_get_attribute_by_id(driver, 'mAbsLen', 'value')
        assert "0" == conftest.element_get_attribute_by_id(driver, 'mFigs', 'value')

    def test_short(self):
        driver = self.driver

        # Fill in existing conference short and save
        conftest.element_send_keys_by_id(driver, 'short', 'BC14')
        conftest.element_click_by_class_name(driver, 'btn-success')

        xpath_alert = '//div[contains(@class,"alert-danger")]/strong'
        # Wait for save alert to be displayed
        self.wait_until(30, EC.presence_of_element_located((By.XPATH, xpath_alert)))
        # Check save alert is displayed
        test_msg = "Conference short is already in use."
        assert test_msg in driver.find_element_by_xpath(xpath_alert).text

        # Fill in new conference short and save
        conf_id = "TC%s" % str(uuid1())[0:10]
        conftest.element_send_keys_by_id(driver, 'short', conf_id)
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check absence of save alert
        self.wait_until(30, EC.invisibility_of_element_located((By.XPATH, xpath_alert)))
        assert len(driver.find_elements_by_xpath(xpath_alert)) == 0

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Check conference is present by short id and contains the conference name
        xpath_div_conf = '//div[@class="media-body"]/h4[@id="%s" and contains(text(), "Test Conference")]' % conf_id
        assert len(driver.find_elements_by_xpath(xpath_div_conf)) == 1

        # Check new conference is present in the unpublished section
        xpath_div = '//div[@class="media-body"]/h4[@id="%s"]/..' % conf_id
        assert 'unpublished' in driver.find_element_by_xpath('%s/..' % xpath_div).get_attribute('class')

        # Move back to the conference settings page via link
        driver.find_element_by_xpath('%s//a[contains(text(),"Conference Settings")]' % xpath_div).click()
        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_published(self):
        driver = self.driver

        # Select published checkbox
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'published')))
        conftest.element_click_by_id(driver, 'published')

        # Save conference
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Check that the conference is present by conference id
        xpath_div_conf = '//div[@class="media-body"]/a[contains(@href,"%s")]' % conf_id
        assert len(driver.find_elements_by_xpath(xpath_div_conf)) == 1

        conf_div = driver.find_element_by_xpath(xpath_div_conf + '/..')

        # Check the conference is not in the unpublished section
        assert 'unpublished' not in conf_div.find_element_by_xpath('..').get_attribute('class')
        # Check admin manage abstracts link is available
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Manage abstracts")]')) == 1
        # Check admin conference settings link is available
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Conference Settings")]')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_thumbnail_url(self):
        driver = self.driver

        # The thumbnail is only shown if a conference is not active
        img_thumb = "https://portal.g-node.org/abstracts/bc14/BC14_icon.png"

        # Fill in thumbnail link
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'thumbnail')))
        conftest.element_send_keys_by_id(driver, 'thumbnail', img_thumb)

        # Save conference
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Select conference by conference id
        xpath_div_conf = '//div[@class="media-body"]/a[contains(@href,"%s")]/..' % conf_id
        conf_div = driver.find_element_by_xpath(xpath_div_conf)

        # Check conference thumbnail displayed
        xpath_image = '..//div[contains(@class,"media-left")]//a/img[@src="%s"]' % img_thumb
        assert len(conf_div.find_elements_by_xpath(xpath_image)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_active(self):
        driver = self.driver

        # Select 'active' checkbox
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'active')))
        conftest.element_click_by_id(driver, 'active')

        # Save conference
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Find conference via conference id and check its in the jumbotron (active conference) section
        xpath_div_conf = '//div[@class="jumbotron"]/h3/a[contains(@href,"%s")]' % conf_id
        assert len(driver.find_elements_by_xpath(xpath_div_conf)) == 1

        conf_div = driver.find_element_by_xpath('%s/../..' % xpath_div_conf)
        # Check admin manage abstracts link is available
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Manage")]')) == 1
        # Check admin conference settings link is available
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Conference Settings")]')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_submission(self):
        driver = self.driver

        # Fetch conference id from conference settings page
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'short')))
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')

        # Switch to conference start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + conf_id)

        # Check that there is no submission text
        xpath_submission = '//ul[contains(@class,"nav")]//li/a[contains(text(),"Submission")]'
        assert len(driver.find_elements_by_xpath(xpath_submission)) == 0
        # Check that there is no submission submit abstract link
        xpath_submit = '//div[@class="jumbotron"]//a[contains(text(),"Submit")]'
        assert len(driver.find_elements_by_xpath(xpath_submit)) == 0

        # Switch back to the conference settings page via the conference settings link
        xpath_conf_setting = '//div[@class="jumbotron"]//a[contains(text(),"Conference Settings")]'
        conftest.element_click_by_xpath(driver, xpath_conf_setting)

        # Select 'submission' checkbox
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'submission')))
        conftest.element_click_by_id(driver, 'submission')

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to conference start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + conf_id)

        # Check that there is a submission text
        assert len(driver.find_elements_by_xpath(xpath_submission)) == 1
        # Check that there is a submission submit abstract link
        assert len(driver.find_elements_by_xpath(xpath_submit)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_group(self):
        driver = self.driver

        # Fill in conference group
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'group')))
        test_val = "Test Group 1"
        conftest.element_send_keys_by_id(driver, 'group', test_val)

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check value has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'group')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="group"]', 'value')

    def test_cite(self):
        driver = self.driver

        # Fill in conference citation text
        test_val = "The Test Conf, Somewhere, Sometime"
        conftest.element_send_keys_by_id(driver, 'cite', test_val)

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check value has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'cite')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="cite"]', 'value')

    def test_start_date(self):
        driver = self.driver

        # Select start date via date picker to the 14. of the next month
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'start')))
        test_val = "14"
        conftest.element_click_by_id(driver, 'start')

        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "%s")]' % test_val).click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check that the date has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'start')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="start"]', 'value')

        # Check start date cannot be set to invalid values
        old_date = conftest.element_get_attribute_by_id(driver, 'start', 'value')
        # Check invalid date
        conftest.element_send_keys_by_id(driver, 'start', '99/99/9999')
        # Switch focus
        conftest.element_click_by_id(driver, 'mFigs')

        # Check date value has not been changed
        assert old_date in conftest.element_get_attribute_by_id(driver, 'start', 'value')

        # Check date cannot be set to invalid string
        driver.find_element_by_id('start').send_keys('hello')
        assert old_date in conftest.element_get_attribute_by_id(driver, 'start', 'value')

        # Switch to start page
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Select conference by conference id
        div_conf = '//div[@class="jumbotron"]/h3/a[contains(@href,"%s")]/../..' % conf_id
        conf_div = driver.find_element_by_xpath(div_conf)

        # Check conference contains changed start date
        assert len(conf_div.find_elements_by_xpath('./p[contains(text(),"%s")]' % test_val)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_end_date(self):
        driver = self.driver

        # Select end date via date picker to the 16. of the next month
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'end')))
        test_val = "16"
        conftest.element_click_by_id(driver, 'end')

        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "%s")]' % test_val).click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check that date has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'end')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="end"]', 'value')

        # Check date cannot be set to invalid values
        old_date = conftest.element_get_attribute_by_id(driver, 'end', 'value')
        # Check invalid date
        conftest.element_send_keys_by_id(driver, 'end', '99/99/9999')
        # Switch focus
        conftest.element_click_by_id(driver, 'mFigs')

        # Check date has not been changed
        assert old_date in conftest.element_get_attribute_by_id(driver, 'end', 'value')

        # Check date cannot be set to invalid string
        driver.find_element_by_id('end').send_keys('hello')
        assert old_date in conftest.element_get_attribute_by_id(driver, 'end', 'value')

        # Switch to start page
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Select conference by conference id
        xpath_div_conf = '//div[@class="jumbotron"]/h3/a[contains(@href,"' + conf_id + '")]/../..'
        conf_div = driver.find_element_by_xpath(xpath_div_conf)

        # Check conference contains changed end date
        assert len(conf_div.find_elements_by_xpath('./p[contains(text(),"%s")]' % test_val)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_deadline(self):
        driver = self.driver

        # Select deadline date via date picker to the 10. of the next month
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'deadline')))
        test_val = "10"
        conftest.element_click_by_id(driver, 'deadline')
        driver.find_element_by_id('deadline').click()

        driver.find_element_by_xpath('//a[contains(@class,"ui-datepicker-next")]').click()
        driver.find_element_by_xpath('//div[@id="ui-datepicker-div"]//a[contains(text(), "%s")]' % test_val).click()
        driver.find_element_by_xpath('//button[contains(@class, "datepicker-close")]').click()

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        self.wait_until(30, EC.presence_of_element_located((By.ID, 'deadline')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="deadline"]', 'value')

        old_date = conftest.element_get_attribute_by_id(driver, 'deadline', 'value')
        conftest.element_send_keys_by_id(driver, 'deadline', '99/99/9999')
        conftest.element_click_by_id(driver, "mFigs")

        assert old_date in conftest.element_get_attribute_by_id(driver, 'deadline', 'value')
        driver.find_element_by_id('deadline').send_keys('hello')
        conftest.element_click_by_id(driver, "mFigs")
        assert old_date in conftest.element_get_attribute_by_id(driver, 'deadline', 'value')
        conftest.element_click_by_id(driver, "mFigs")

    def test_logo_url(self):
        driver = self.driver

        # Conference banner url
        img_header = 'https://portal.g-node.org/abstracts/bc18/BC18_header.jpg'

        # Set conference banner url
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'logo')))
        conftest.element_send_keys_by_id(driver, 'logo', img_header)

        # Save conference
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/")

        # Select conference div by conference id
        xpath_tcnum = '//div[@class="jumbotron"]/h3/a[contains(@href,"%s")]/../..' % conf_id
        conf_div = driver.find_element_by_xpath(xpath_tcnum)

        # Check the banner is displayed
        xpath_img_header = './/p[contains(@data-bind,"logo")]//img[@src="%s"]' % img_header
        assert len(conf_div.find_elements_by_xpath(xpath_img_header)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_iosapp(self):
        driver = self.driver

        # Fill in iosapp entry
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'iosapp')))
        test_val = "999999999"
        conftest.element_send_keys_by_id(driver, 'iosapp', test_val)

        # Save conference
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check value has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'iosapp')))
        assert test_val in conftest.element_get_attribute_by_xpath(driver, '//*[@id="iosapp"]', 'value')

    def test_link(self):
        driver = self.driver

        # Fill in conference link
        conf_link = 'http://www.nncn.de/en/bernstein-conference/2014'
        conftest.element_send_keys_by_id(driver, 'link', conf_link)

        # Save conference
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to conference main page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + conf_id)

        # Check the conference banner contains the set conference link
        xpath_link = '//div[@class="jumbotron"]/div[contains(@data-bind,"logo")]/a[contains(@href,"%s")]' % conf_link
        assert len(driver.find_elements_by_xpath(xpath_link)) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_description(self):
        driver = self.driver

        # Fill in conference description
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'desc')))
        test_val = "Important conference."
        conftest.element_send_keys_by_id(driver, 'desc', test_val)

        # Save conference settings
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000")

        # Select conference by conference id
        xpath_conf_div = '//div[@class="jumbotron"]/h3/a[contains(@href,"%s")]/../..' % conf_id
        conf_div = driver.find_element_by_xpath(xpath_conf_div)

        # Check conference jumbotron contains the description
        xpath_para = './div[@class="jumbo-small"]/p[contains(text(),"%s")]' % test_val
        assert len(conf_div.find_elements_by_xpath(xpath_para)) == 1

        # Switch to conference main page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + conf_id)

        # Check the conference description is displayed
        xpath_para = '//div[@class="jumbotron"]/div[@class="jumbo-small"]/p[@class="paragraph-small"]'
        assert test_val in driver.find_element_by_xpath(xpath_para).text

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_notice(self):
        # Test conference wide note
        driver = self.driver

        # Fill in conference note
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'notice')))
        test_val = 'Check abstracts before submission!'
        conftest.element_send_keys_by_id(driver, 'notice', test_val)

        # Save conference settings
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to conference main page
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + conf_id)

        # Check notice is displayed as first jumbotron
        xpath_check = '//div[@class="jumbotron"]//p[contains(text(),"%s")]' % test_val
        assert len(driver.find_elements_by_xpath(xpath_check)) == 1

        # Switch to conference abstracts page
        driver.get("http://%s:9000/conference/%s/abstracts" % (Cookies.get_host_ip(), conf_id))

        # Check notice is displayed as first jumbotron
        xpath_check = '//div[@class="jumbotron"]//p[contains(text(),"%s")]' % test_val
        assert len(driver.find_elements_by_xpath(xpath_check)) == 1

        # Switch to start page
        driver.get("http://" + Cookies.get_host_ip() + ":9000")

        # Check notice is not displayed
        xpath_check = '//div[@class="jumbotron"]//p[contains(text(),"%s")]' % test_val
        assert len(driver.find_elements_by_xpath(xpath_check)) == 0

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_presentation_preferences(self):
        driver = self.driver

        # Select presentation check box
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'presentation')))
        conftest.element_click_by_id(driver, 'presentation')

        # Save conference settings
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        url_submission = "http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id)
        url_conf_settings = "http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id)

        # Switch to the conference abstract submission page
        driver.get(url_submission)

        # Check that the presentation type option is available
        assert len(driver.find_elements_by_class_name('poster-or-talk')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_add_topic(self):
        driver = self.driver

        # Add an abstract topic
        self.wait_until(30, EC.presence_of_element_located((By.ID, 'addTopic')))
        conftest.element_send_keys_by_id(driver, 'addTopic', 'Topic A')
        driver.find_element_by_id('btn-add-topic').click()

        # Check topic has been added
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic A")]')) == 1

        # Add second topic
        conftest.element_send_keys_by_id(driver, 'addTopic', 'Topic B')
        driver.find_element_by_id('btn-add-topic').click()
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic B")]')) == 1

        # Save conference settings
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to the conference abstract submission page
        driver.get("http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id))

        # Open abstract topic modal
        conftest.move_to_element_by_class_name(driver, 'topic')
        self.wait_until(10, EC.visibility_of_element_located((By.ID, 'button-edit-topic')))
        driver.find_element_by_id('button-edit-topic').click()

        # Check there are two options available
        xpath_radio = '//*[@id="topic-editor"]//div[contains(@class, "radio")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_radio)))

        xpath_radio_input = '//*[@id="topic-editor"]//div[contains(@class, "radio")]//input'
        assert len(driver.find_elements_by_xpath(xpath_radio_input)) == 2

        # Close topic modal
        driver.find_element_by_xpath('//*[@id="topic-editor"]//button[@id="modal-button-ok"]').click()

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_remove_topic(self):
        driver = self.driver

        self.wait_until(30, EC.presence_of_element_located((By.ID, 'addTopic')))
        val_remove = "Topic A"
        val_remain = "Topic B"

        # Remove first topic
        xpath_topic_link = '//ul/li/span[contains(text(),"%s")]/../a' % val_remove
        driver.find_element_by_xpath(xpath_topic_link).click()

        # Check topic has been removed
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"%s")]' % val_remove)) == 0
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"%s")]' % val_remain)) == 1

        # Save conference settings
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Switch to the conference abstract submission page
        driver.get("http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id))

        # Open abstract topic modal
        conftest.move_to_element_by_class_name(driver, 'topic')
        self.wait_until(10, EC.visibility_of_element_located((By.ID, 'button-edit-topic')))
        driver.find_element_by_id('button-edit-topic').click()

        xpath_radio = '//*[@id="topic-editor"]//div[contains(@class, "radio")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_radio)))

        # Check that there is one option available
        xpath_radio_input = '//*[@id="topic-editor"]//div[contains(@class, "radio")]//input'
        assert len(driver.find_elements_by_xpath(xpath_radio_input)) == 1

        xpath_radio_span = '//*[@id="topic-editor"]//div[contains(@class, "radio")]//span'
        assert val_remain in driver.find_element_by_xpath(xpath_radio_span).text
        assert val_remove not in driver.find_element_by_xpath(xpath_radio_span).text

        # Close topic modal
        driver.find_element_by_xpath('//*[@id="topic-editor"]//button[@id="modal-button-ok"]').click()

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_maximum_abstract_length(self):
        driver = self.driver

        abstract_id = 'mAbsLen'
        self.wait_until(30, EC.presence_of_element_located((By.ID, abstract_id)))

        # Set maximum abstract length
        test_abstract_len = "300"
        conftest.element_send_keys_by_id(driver, abstract_id, test_abstract_len)

        # Check warning message when a set abstract length is shortened
        test_msg = "Changing to shorter abstract length causes cut-offs"
        xpath_alert = '//div[contains(@class,"form-group")]/div[contains(@class,"alert-danger")]'
        assert test_msg in driver.find_element_by_xpath(xpath_alert).text

        # Save conference settings
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check abstract length has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, abstract_id)))
        assert test_abstract_len == driver.find_element_by_id(abstract_id).get_attribute('value')

        # Switch to the conference abstract submission page
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id))

        # Open abstract text modal
        conftest.move_to_element_by_class_name(driver, 'abstract-text')
        self.wait_until(10, EC.visibility_of_element_located((By.ID, 'button-edit-abstract-text')))
        driver.find_element_by_id('button-edit-abstract-text').click()

        self.wait_until(30, EC.visibility_of_element_located((By.XPATH, '//*[@id="abstract-text-editor"]')))

        # Check maximum length message
        xpath_text = '//*[@id="abstract-text-editor"]//textarea[@id="text"]'
        assert driver.find_element_by_xpath(xpath_text).get_attribute("maxlength") == test_abstract_len

        # Close abstract text modal
        xpath_modal = '//*[@id="abstract-text-editor"]//button[@id="modal-button-ok"]'
        driver.find_element(By.XPATH, xpath_modal).click()

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_maximum_figures(self):
        driver = self.driver

        # Switch to the conference abstract submission page
        self.wait_until(10, EC.visibility_of_element_located((By.ID, 'short')))
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id))

        # Check figure modal is not available
        assert len(driver.find_elements_by_class_name('figure')) == 0

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

        # Set abstract figure number
        fig_id = "mFigs"
        test_fig_num = "3"
        self.wait_until(30, EC.presence_of_element_located((By.ID, fig_id)))
        conftest.element_send_keys_by_id(driver, fig_id, test_fig_num)

        # Save conference settings
        conftest.element_click_by_class_name(driver, 'btn-success')

        # Check figure number has been set
        self.wait_until(30, EC.presence_of_element_located((By.ID, fig_id)))
        assert test_fig_num == driver.find_element_by_id(fig_id).get_attribute('value')

        # Switch to the conference abstract submission page
        conf_id = conftest.element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://%s:9000/conference/%s/submission" % (Cookies.get_host_ip(), conf_id))

        # Check figure section is available
        assert len(driver.find_elements_by_class_name('figure')) == 1

        # Open figure modal
        conftest.move_to_element_by_class_name(driver, 'figure')
        self.wait_until(10, EC.visibility_of_element_located((By.ID, 'button-edit-figure')))
        driver.find_element_by_id('button-edit-figure').click()

        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, '//*[@id="figures-editor"]')))

        # Check figure number
        xpath_fig_len = '//*[@id="figures-editor"]//div[@class="modal-body"]' \
                        '//div[contains(@data-bind, "figures().length>=%s")]' % test_fig_num
        assert len(driver.find_elements_by_xpath(xpath_fig_len)) == 1

        # Close figure modal
        xpath_modal = '//*[@id="figures-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_modal).click()

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), conf_id))

    def test_groups(self):
        driver = self.driver

        # Set conference abstract groups
        xpath_groups = '//li/a[contains(@href,"groups")]'
        self.wait_until(30, EC.presence_of_element_located((By.XPATH, xpath_groups)))

        # Switch to conference groups tab
        driver.find_element_by_xpath(xpath_groups).click()

        # Add empty group
        xpath_btn_new_group = '//tr[@id="newGroup"]//td/button'
        driver.find_element_by_xpath(xpath_btn_new_group).click()

        # Check that empty group cannot be saved
        xpath_alert = '//div[contains(@class,"alert-danger")]/strong'
        test_msg_full_entry = "Prefix, short and long entries have to be provided!"
        assert test_msg_full_entry in driver.find_element_by_xpath(xpath_alert).text

        # Set all fields of the added group entry to invalid values
        id_prefix = "ngPrefix"
        id_short = "ngShort"
        id_name = "ngName"
        driver.find_element_by_id(id_prefix).send_keys("u")
        driver.find_element_by_id(id_short).send_keys("3")
        driver.find_element_by_id(id_name).send_keys("3")

        # Check invalid values cannot be saved
        driver.find_element_by_xpath(xpath_btn_new_group).click()

        # Check invalid prefix message
        test_msg_num_only = "Prefix can only contain numbers!"
        assert test_msg_num_only in driver.find_element_by_xpath(xpath_alert).text

        # Change prefix to invalid content
        conftest.element_send_keys_by_id(driver, id_prefix, '1.2')
        driver.find_element_by_xpath(xpath_btn_new_group).click()
        assert test_msg_num_only in driver.find_element_by_xpath(xpath_alert).text

        # Change prefix to valid content
        conftest.element_send_keys_by_id(driver, id_prefix, '1')
        driver.find_element_by_xpath(xpath_btn_new_group).click()

        # Check long name invalid content
        test_msg_name_char = "Name cannot contain only numbers!"
        assert test_msg_name_char in driver.find_element_by_xpath(xpath_alert).text

        # Change long name to valid content
        conftest.element_send_keys_by_id(driver, id_name, 'Group 1')
        driver.find_element_by_xpath(xpath_btn_new_group).click()

        # Check short name invalid content
        test_msg_short_char = "Short cannot contain only numbers!"
        assert test_msg_short_char in driver.find_element_by_xpath(xpath_alert).text

        # Change short name to valid content
        conftest.element_send_keys_by_id(driver, id_short, 'G1')
        driver.find_element_by_xpath(xpath_btn_new_group).click()

        assert len(driver.find_elements_by_xpath(xpath_alert)) == 0

        xpath_btn_success = '//*[@id="groups"]/div/button[contains(@class,"btn-success")]'
        driver.find_element_by_xpath(xpath_btn_success).click()

        assert len(driver.find_elements_by_xpath(xpath_alert)) == 0
