import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from conftest import move_to_element_by_class_name


@pytest.mark.usefixtures("setup_editor")
class TestEditor:

    def wait_until(self, delay, condition):
        WebDriverWait(self.driver, delay).until(condition)

    def click_edit_button(self, name):
        """
        Helper method to select and use specific modal edit button.

        :param name: id of the modal edit button
        """
        driver = self.driver
        if 'firefox' in driver.capabilities['browserName'] and name != 'title':
            move_to_element_by_class_name(driver, 'title')

        move_to_element_by_class_name(driver, name)

        element_id = 'button-edit-' + name
        self.wait_until(10, EC.visibility_of_element_located((By.ID, element_id)))
        driver.find_element_by_id(element_id).click()

    def test_simple_creation(self):
        driver = self.driver

        # fail saving on an empty abstract
        driver.find_element_by_id('button-action').click()
        assert EC.visibility_of((By.XPATH, '/html/body/div[2]/div[2]/div[3]/div/h4'))

    def test_title(self):
        driver = self.driver

        # Open title modal
        self.click_edit_button('title')

        # Wait for modal to open; xpath is required since there are several copies
        # in the source code
        wait_on_path = '//*[@id="title-editor"]'
        self.wait_until(30, EC.visibility_of_element_located((By.XPATH, wait_on_path)))

        # Select title input field and set new value
        title = driver.find_element_by_xpath('//*[@id="title-editor"]//input[@id="title"]')
        title.send_keys('New Test Abstract')

        # Close modal
        xpath_btn_modal = '//*[@id="title-editor"]//button[@id="modal-button-ok"]'
        driver.find_element(By.XPATH, xpath_btn_modal).click()

        # Save abstract
        self.wait_until(30, EC.element_to_be_clickable((By.ID, 'button-action')))
        driver.find_element_by_id('button-action').click()

        # Make sure that abstract issues are displayed
        self.wait_until(30, EC.text_to_be_present_in_element((By.ID, 'lblvalid'), 'issues'))

        # Make sure that submit fails
        driver.find_element_by_id('button-action').click()

        check_xpath = '/html/body/div[2]/div[2]/div[3]/div/p'
        assert EC.text_to_be_present_in_element((By.XPATH, check_xpath), 'Unable to submit')

    def test_presentation_type(self):
        driver = self.driver

        # Open presentation type model
        self.click_edit_button('poster-or-talk')

        wait_on_path = '//*[@id="is-talk-editor"]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, wait_on_path)))

        # Test selecting 'Poster' as presentation type
        xpath_radio = '//*[@id="is-talk-editor"]//div[contains(@class, "radio")][2]//input'
        driver.find_element_by_xpath(xpath_radio).click()

        # Close modal
        xpath_btn_modal = '//*[@id="is-talk-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Poster" in driver.find_element_by_xpath('//*[@class="poster-or-talk"]//span').text

    def test_add_author(self):
        driver = self.driver

        # Open authors modal
        self.click_edit_button('authors')

        xpath_btn_add = '//*[@id="authors-editor"]//button[contains(@class, "btn-add")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_add)))

        # Test adding full author
        driver.find_element_by_xpath(xpath_btn_add).click()

        xpath_fname = '//*[@id="authors-editor"]//input[contains(@class, "first-name")]'
        xpath_mname = '//*[@id="authors-editor"]//input[contains(@class, "middle-name")]'
        xpath_lname = '//*[@id="authors-editor"]//input[contains(@class, "last-name")]'
        xpath_email = '//*[@id="authors-editor"]//input[contains(@class, "author-mail")]'

        driver.find_element_by_xpath(xpath_fname).send_keys("Alice")
        driver.find_element_by_xpath(xpath_mname).send_keys("Bianca")
        driver.find_element_by_xpath(xpath_lname).send_keys("Foo")
        driver.find_element_by_xpath(xpath_email).send_keys("alice@example.com")

        # Test adding author, missing middle name
        driver.find_element_by_xpath(xpath_btn_add).click()

        xpath_fname = '//*[@id="authors-editor"]//tr[2]//input[contains(@class, "first-name")]'
        xpath_lname = '//*[@id="authors-editor"]//tr[2]//input[contains(@class, "last-name")]'
        xpath_email = '//*[@id="authors-editor"]//tr[2]//input[contains(@class, "author-mail")]'

        driver.find_element_by_xpath(xpath_fname).send_keys("Bob")
        driver.find_element_by_xpath(xpath_lname).send_keys("Bar")
        driver.find_element_by_xpath(xpath_email).send_keys("bob@example.com")

        # Test adding author, missing email
        driver.find_element_by_xpath(xpath_btn_add).click()

        xpath_fname = '//*[@id="authors-editor"]//tr[3]//input[contains(@class, "first-name")]'
        xpath_lname = '//*[@id="authors-editor"]//tr[3]//input[contains(@class, "last-name")]'

        driver.find_element_by_xpath(xpath_fname).send_keys("Charlie")
        driver.find_element_by_xpath(xpath_lname).send_keys("Comma")

        # Close modal
        xpath_btn_modal = '//*[@id="authors-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Alice" in driver.find_element_by_xpath('//*[@class="authors"]//li[1]/span').text
        assert "Bob" in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text
        assert "Charlie" in driver.find_element_by_xpath('//*[@class="authors"]//li[3]/span').text

    def test_remove_author(self):
        driver = self.driver

        # Open authors modal
        self.click_edit_button('authors')

        wait_on_path = '//*[@id="authors-editor"]//button[contains(@class, "btn-add")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, wait_on_path)))

        # Remove author
        xpath_btn_rm = '//*[@id="authors-editor"]//tr[2]//button[contains(@class, "btn-remove")]'
        driver.find_element_by_xpath(xpath_btn_rm).click()

        # Close modal
        xpath_btn_modal = '//*[@id="authors-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Alice" in driver.find_element_by_xpath('//*[@class="authors"]//li[1]/span').text
        assert "Bob" not in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text
        assert "Charlie" in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text
        assert len(driver.find_elements_by_xpath('//*[@class="authors"]//li[3]/span')) == 0

    def test_add_affiliations(self):
        driver = self.driver

        # Open affiliations modal
        self.click_edit_button('affiliations')

        xpath_btn_affil = '//*[@id="affiliations-editor"]//button[contains(@class, "btn-add")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_affil)))

        # Add a full affiliation
        driver.find_element_by_xpath(xpath_btn_affil).click()

        xpath_dep = '//*[@id="affiliations-editor"]//input[contains(@class, "affil-department")]'
        xpath_inst = '//*[@id="affiliations-editor"]//input[contains(@class, "affil-institution")]'
        xpath_address = '//*[@id="affiliations-editor"]//input[contains(@class, "affil-address")]'
        xpath_country = '//*[@id="affiliations-editor"]//input[contains(@class, "affil-country")]'

        driver.find_element_by_xpath(xpath_dep).send_keys("Bio")
        driver.find_element_by_xpath(xpath_inst).send_keys("University")
        driver.find_element_by_xpath(xpath_address).send_keys("Sesame Street 135a")
        driver.find_element_by_xpath(xpath_country).send_keys("Invenden")

        # Add affiliation to an author; select author from dropdown
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//select/option[1]').click()
        # Add selected author
        xpath_btn_asgn = '//*[@id="affiliations-editor"]//button[@id="button-assign-affiliation-to-author"]'
        driver.find_element_by_xpath(xpath_btn_asgn).click()

        # Close modal
        xpath_btn_modal = '//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Bio" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text

        # Open affiliations modal
        self.click_edit_button('affiliations')

        xpath_btn_affil = '//*[@id="affiliations-editor"]//button[contains(@class, "btn-add")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_affil)))

        # Add a new affiliation
        driver.find_element_by_xpath(xpath_btn_affil).click()

        xpath_dep = '//*[@id="affiliations-editor"]//tr[4]//input[contains(@class, "affil-department")]'
        xpath_inst = '//*[@id="affiliations-editor"]//tr[4]//input[contains(@class, "affil-institution")]'
        xpath_address = '//*[@id="affiliations-editor"]//tr[4]//input[contains(@class, "affil-address")]'
        xpath_country = '//*[@id="affiliations-editor"]//tr[4]//input[contains(@class, "affil-country")]'

        driver.find_element_by_xpath(xpath_dep).send_keys("Computational")
        driver.find_element_by_xpath(xpath_inst).send_keys("University")
        driver.find_element_by_xpath(xpath_address).send_keys("Long Street 1-3")
        driver.find_element_by_xpath(xpath_country).send_keys("Fantidan")

        xpath_btn_asgn = '//*[@id="affiliations-editor"]//tr[6]//button[@id="button-assign-affiliation-to-author"]'
        # Assign author one
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]//select/option[1]').click()
        driver.find_element_by_xpath(xpath_btn_asgn).click()

        # Assign author two
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]//select/option[2]').click()
        driver.find_element_by_xpath(xpath_btn_asgn).click()

        # Close modal
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Computational" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[2]/span').text

    def test_remove_affiliations(self):
        driver = self.driver

        # Open affiliations modal
        self.click_edit_button('affiliations')

        xpath_btn_rm = '//*[@id="affiliations-editor"]//tr[1]//button[contains(@class, "btn-remove")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_rm)))

        # Remove affiliation
        driver.find_element_by_xpath(xpath_btn_rm).click()

        # Close modal
        xpath_btn_modal = '//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Bio" not in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text
        assert len(driver.find_elements_by_xpath('//*[@class="affiliations"]//li[2]/span')) == 0

        # Open affiliations modal
        self.click_edit_button('affiliations')

        # Remove author from affiliation
        xpath_btn_author = '//*[@id="affiliations-editor"]' \
                           '//button[contains(@class, "button-remove-affiliation-from-author")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_author)))
        driver.find_element_by_xpath(xpath_btn_author).click()

        # Close modal
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "Computational" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text

    def test_text(self):
        driver = self.driver

        # Open abstracts text modal
        self.click_edit_button('abstract-text')

        wait_on_path = '//*[@id="abstract-text-editor"]'
        self.wait_until(30, EC.visibility_of_element_located((By.XPATH, wait_on_path)))

        # Add abstract text value
        test_content = "Test abstract test."
        text = driver.find_element_by_xpath('//*[@id="abstract-text-editor"]//textarea[@id="text"]')
        text.send_keys(test_content)

        # Close modal
        xpath_btn_modal = '//*[@id="abstract-text-editor"]//button[@id="modal-button-ok"]'
        driver.find_element(By.XPATH, xpath_btn_modal).click()

        assert test_content in driver.find_element_by_xpath('//*[@class="abstract-text"]/p').text

    def test_acknowledgements(self):
        driver = self.driver

        # Open acknowledgements modal
        self.click_edit_button('acknowledgements')

        wait_on_path = '//*[@id="acknowledgements-editor"]'
        self.wait_until(30, EC.visibility_of_element_located((By.XPATH, wait_on_path)))

        # Add acknowledgements value
        test_content = "Thanks."
        xpath_ack = '//*[@id="acknowledgements-editor"]//textarea[@id="acknowledgements"]'
        acknowledgements = driver.find_element_by_xpath(xpath_ack)
        acknowledgements.send_keys(test_content)

        # Close modal
        xpath_btn_modal = '//*[@id="acknowledgements-editor"]//button[@id="modal-button-ok"]'
        driver.find_element(By.XPATH, xpath_btn_modal).click()

        assert test_content in driver.find_element_by_xpath('//*[@class="acknowledgements"]/p').text

    def test_add_references(self):
        driver = self.driver

        # Open references modal
        self.click_edit_button('references')

        xpath_btn_add_ref = '//*[@id="references-editor"]//button[contains(@class, "btn-add")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_add_ref)))

        xpath_citation = '//*[@id="references-editor"]//input[contains(@class, "citation")]'
        xpath_link = '//*[@id="references-editor"]//input[contains(@class, "link")]'
        xpath_doi = '//*[@id="references-editor"]//input[contains(@class, "doi")]'

        # Test invalid reference citation
        driver.find_element_by_xpath(xpath_btn_add_ref).click()
        driver.find_element_by_xpath(xpath_citation).send_keys("5657")

        # Close modal
        xpath_btn_modal = '//*[@id="references-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        # Check an error callout is displayed
        assert driver.find_element_by_xpath('//div[contains(@class, "callout")]/p')

        # Check correct error message
        test_msg = "Reference citation"
        assert EC.text_to_be_present_in_element((By.XPATH, '//div[contains(@class, "callout")]/p'), test_msg)

        # Open references modal
        self.click_edit_button('references')

        # Test invalid reference link
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_add_ref)))
        driver.find_element_by_xpath(xpath_btn_add_ref).click()

        # Add valid reference citation value
        driver.find_element_by_xpath(xpath_citation).send_keys("John, title.")
        # Add invalid reference link value
        driver.find_element_by_xpath(xpath_link).send_keys("123")

        # Close modal
        xpath_btn_modal = '//*[@id="references-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        # Check an error callout is displayed
        assert driver.find_element_by_xpath('//div[contains(@class, "callout")]/p')

        # Check correct error message
        test_msg = "Reference link"
        assert EC.text_to_be_present_in_element((By.XPATH, '//div[contains(@class, "callout")]/p'), test_msg)

        # Open references modal
        self.click_edit_button('references')

        # Test invalid reference doi
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_add_ref)))
        driver.find_element_by_xpath(xpath_btn_add_ref).click()

        # Add valid reference link value
        driver.find_element_by_xpath(xpath_link).send_keys("www.link.com")
        # Add invalid reference doi value
        driver.find_element_by_xpath(xpath_doi).send_keys("123")

        # Close modal
        driver.find_element_by_xpath(xpath_btn_modal).click()

        # Check an error callout is displayed
        assert driver.find_element_by_xpath('//div[contains(@class, "callout")]/p')

        # Check correct error message
        test_msg = "Reference doi"
        assert EC.text_to_be_present_in_element((By.XPATH, '//div[contains(@class, "callout")]/p'), test_msg)

        # Open references modal
        self.click_edit_button('references')

        # Test valid reference entry
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_add_ref)))
        driver.find_element_by_xpath(xpath_btn_add_ref).click()

        driver.find_element_by_xpath(xpath_citation).send_keys("John, title.")
        driver.find_element_by_xpath(xpath_link).send_keys("www.link.com")
        driver.find_element_by_xpath(xpath_doi).send_keys("doi:0000000/000000000000")

        # Close modal
        driver.find_element_by_xpath(xpath_btn_modal).click()

        wait_on_path = '//*[@class="references"]//a'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, wait_on_path)))

        # Check absence of error callout
        assert len(driver.find_elements_by_xpath('//div[contains(@class, "callout")]/p')) == 0

    def test_remove_references(self):
        driver = self.driver

        # Open references modal
        self.click_edit_button('references')

        xpath_btn_rm = '//*[@id="references-editor"]//tr[1]//button[contains(@class, "btn-remove")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, xpath_btn_rm)))

        # Remove reference
        driver.find_element_by_xpath(xpath_btn_rm).click()

        # Close modal
        xpath_btn_modal = '//*[@id="references-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        # Check absence of error callout
        assert len(driver.find_elements_by_xpath('//*[@class="references"]//a')) == 0

    def test_topic(self):
        driver = self.driver

        # Open topic modal
        self.click_edit_button('topic')

        wait_on_path = '//*[@id="topic-editor"]//div[contains(@class, "radio")]'
        self.wait_until(10, EC.element_to_be_clickable((By.XPATH, wait_on_path)))

        # Select topic
        xpath_radio = '//*[@id="topic-editor"]//div[contains(@class, "radio")][2]//input'
        driver.find_element_by_xpath(xpath_radio).click()

        # Close modal
        xpath_btn_modal = '//*[@id="topic-editor"]//button[@id="modal-button-ok"]'
        driver.find_element_by_xpath(xpath_btn_modal).click()

        assert "topic two" in driver.find_element_by_xpath('//*[@class="topic"]/p').text

    def test_submit(self):
        driver = self.driver

        # Submit abstract
        driver.find_element_by_id('button-action').click()

        # Check abstract status
        test_status = "Submitted"
        assert EC.text_to_be_present_in_element((By.XPATH, '//*[@class_name="label-primary"]'), test_status)

    def test_unlock(self):
        driver = self.driver

        # Unlock submitted abstract
        driver.find_element_by_id('button-action').click()

        # Check abstract status
        test_status = "InPreparation"
        assert EC.text_to_be_present_in_element((By.XPATH, '//*[@class_name="label-primary"]'), test_status)
