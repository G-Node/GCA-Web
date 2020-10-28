import pytest
import Cookies
from uuid import uuid1
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from conftest import element_send_keys_by_id, element_send_keys_by_class_name, element_send_keys_by_xpath, \
    element_click_by_id, element_click_by_class_name, element_click_by_xpath, \
    element_get_attribute_by_id, element_get_attribute_by_class_name, element_get_attribute_by_xpath, \
    move_to_element_by_id, move_to_element_by_class_name, move_to_element_by_xpath


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
        assert 'unpublished' in conf_div.find_element_by_xpath('..').get_attribute('class')
        assert len(conf_div.find_elements_by_xpath('./h4[contains(text(),"Test Conference")]')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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
        assert 'unpublished' not in conf_div.find_element_by_xpath('..').get_attribute('class')
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Manage")]')) == 1
        assert len(conf_div.find_elements_by_xpath('.//a[contains(text(),"Conference Settings")]')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

    def test_description(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'desc'))
        )
        element_send_keys_by_id(driver, 'desc', 'Important conference.')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        move_to_element_by_xpath(driver, '//div[@class="jumbotron"]'
                                         '/div[@class="jumbo-small"]/p[@class="paragraph-small"]')
        assert "Important conference." in driver.find_element_by_xpath(
            '//div[@class="jumbotron"]/div[@class="jumbo-small"]/p[@class="paragraph-small"]').text

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

    def test_notice(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'notice'))
        )
        element_send_keys_by_id(driver, 'notice', 'Check abstracts before submission!')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)
        assert len(driver.find_elements_by_xpath('//div[@class="jumbotron"]'
                                                 '//p[contains(text(),"Check abstracts before submission!")]')) == 1

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

    def test_presentation_preferences(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'presentation'))
        )
        element_click_by_id(driver, 'presentation')

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num + "/submission")

        assert len(driver.find_elements_by_class_name('poster-or-talk')) == 1

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')

        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num + "/submission")

        move_to_element_by_class_name(driver, 'topic')
        WebDriverWait(driver, 10).until(
            EC.visibility_of_element_located((By.ID, 'button-edit-topic'))
        )
        driver.find_element_by_id('button-edit-topic').click()

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="topic-editor"]//div[contains(@class, "radio")]'))
        )

        assert len(driver.find_elements_by_xpath('//*[@id="topic-editor"]//div[contains(@class, "radio")]//input')) == 2

        driver.find_element_by_xpath('//*[@id="topic-editor"]//button[@id="modal-button-ok"]').click()

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

    def test_remove_topic(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.ID, 'addTopic'))
        )

        move_to_element_by_xpath(driver, '//ul/li/span[contains(text(),"Topic 1")]/../a')
        driver.find_element_by_xpath('//ul/li/span[contains(text(),"Topic 1")]/../a').click()

        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 1")]')) == 0
        assert len(driver.find_elements_by_xpath('//ul/li/span[contains(text(),"Topic 2")]')) == 1

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        element_click_by_class_name(driver, 'btn-success')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num + "/submission")

        move_to_element_by_class_name(driver, 'topic')
        WebDriverWait(driver, 10).until(
            EC.visibility_of_element_located((By.ID, 'button-edit-topic'))
        )
        driver.find_element_by_id('button-edit-topic').click()

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="topic-editor"]//div[contains(@class, "radio")]'))
        )

        assert len(driver.find_elements_by_xpath('//*[@id="topic-editor"]//div[contains(@class, "radio")]//input')) == 1
        assert "Topic 2" in driver.find_element_by_xpath('//*[@id="topic-editor"]'
                                                         '//div[contains(@class, "radio")]//span').text

        driver.find_element_by_xpath('//*[@id="topic-editor"]//button[@id="modal-button-ok"]').click()

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num + "/submission")

        move_to_element_by_class_name(driver, 'abstract-text')
        WebDriverWait(driver, 10).until(
            EC.visibility_of_element_located((By.ID, 'button-edit-abstract-text'))
        )
        driver.find_element_by_id('button-edit-abstract-text').click()

        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="abstract-text-editor"]'))
        )

        assert driver.find_element_by_xpath('//*[@id="abstract-text-editor"]'
                                            '//textarea[@id="text"]').get_attribute("maxlength") == '300'
        driver.find_element(By.XPATH, '//*[@id="abstract-text-editor"]//button[@id="modal-button-ok"]').click()

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

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

        tc_num = element_get_attribute_by_id(driver, 'short', 'value')
        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num + "/submission")

        move_to_element_by_class_name(driver, 'figure')
        assert len(driver.find_elements_by_class_name('figure')) == 1

        WebDriverWait(driver, 10).until(
            EC.visibility_of_element_located((By.ID, 'button-edit-figure'))
        )
        driver.find_element_by_id('button-edit-figure').click()

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="figures-editor"]'))
        )

        assert len(driver.find_elements_by_xpath('//*[@id="figures-editor"]//div[@class="modal-body"]'
                                                 '//div[contains(@data-bind, "figures().length>=3")]')) == 1

        driver.find_element_by_xpath('//*[@id="figures-editor"]//button[@id="modal-button-ok"]').click()

        driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/" + tc_num)

        # Switch back to the conference settings page
        driver.get("http://%s:9000/dashboard/conference/%s" % (Cookies.get_host_ip(), tc_num))

    def test_groups(self):
        driver = self.driver
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, '//li/a[contains(@href,"groups")]'))
        )

        move_to_element_by_xpath(driver, '//li/a[contains(@href,"groups")]')
        driver.find_element_by_xpath('//li/a[contains(@href,"groups")]').click()

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert "Prefix, short and long entries have to be provided!" in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        move_to_element_by_id(driver, 'ngPrefix')
        driver.find_element_by_id('ngPrefix').send_keys("u")

        move_to_element_by_id(driver, 'ngShort')
        driver.find_element_by_id('ngShort').send_keys("3")

        move_to_element_by_id(driver, 'ngName')
        driver.find_element_by_id('ngName').send_keys("3")

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert "Prefix can only contain numbers!" in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        element_send_keys_by_id(driver, 'ngPrefix', '1.2')

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert "Prefix can only contain numbers!" in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        element_send_keys_by_id(driver, 'ngPrefix', '1')

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert "Name cannot contain only numbers!" in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        element_send_keys_by_id(driver, 'ngName', 'Group 1')

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert "Short cannot contain only numbers!" in \
               driver.find_element_by_xpath('//div[contains(@class,"alert-danger")]/strong').text

        element_send_keys_by_id(driver, 'ngShort', 'G1')

        move_to_element_by_xpath(driver, '//tr[@id="newGroup"]//td/button')
        driver.find_element_by_xpath('//tr[@id="newGroup"]//td/button').click()

        assert len(driver.find_elements_by_xpath('//div[contains(@class,"alert-danger")]/strong')) == 0

        move_to_element_by_xpath(driver, '//*[@id="groups"]/div/button[contains(@class,"btn-success")]')
        driver.find_element_by_xpath('//*[@id="groups"]/div/button[contains(@class,"btn-success")]').click()

        assert len(driver.find_elements_by_xpath('//div[contains(@class,"alert-danger")]/strong')) == 0
