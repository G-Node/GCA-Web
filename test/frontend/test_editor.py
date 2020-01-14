import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
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

    def test_add_author(self):
        driver = self.driver
        self.click_edit_button('authors')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="authors-editor"]//button[contains(@class, "btn-add")]'))
        )

        driver.find_element_by_xpath('//*[@id="authors-editor"]//button[contains(@class, "btn-add")]').click()

        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//input[contains(@class, "first-name")]').send_keys("Alice")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//input[contains(@class, "middle-name")]').send_keys("Bianca")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//input[contains(@class, "last-name")]').send_keys("Foo")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//input[contains(@class, "author-mail")]').send_keys("alice@example.com")

        driver.find_element_by_xpath('//*[@id="authors-editor"]//button[contains(@class, "btn-add")]').click()
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//tr[2]//input[contains(@class, "first-name")]').send_keys("Bob")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//tr[2]//input[contains(@class, "last-name")]').send_keys("Bar")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//tr[2]//input[contains(@class, "author-mail")]').send_keys("bob@example.com")

        driver.find_element_by_xpath('//*[@id="authors-editor"]//button[contains(@class, "btn-add")]').click()
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//tr[3]//input[contains(@class, "first-name")]').send_keys("Charlie")
        driver.find_element_by_xpath('//*[@id="authors-editor"]'
                                     '//tr[3]//input[contains(@class, "last-name")]').send_keys("Comma")

        driver.find_element_by_xpath('//*[@id="authors-editor"]//button[@id="modal-button-ok"]').click()

        assert "Alice" in driver.find_element_by_xpath('//*[@class="authors"]//li[1]/span').text
        assert "Bob" in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text
        assert "Charlie" in driver.find_element_by_xpath('//*[@class="authors"]//li[3]/span').text

    def test_remove_author(self):
        driver = self.driver
        self.click_edit_button('authors')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="authors-editor"]//button[contains(@class, "btn-add")]'))
        )

        driver.find_element_by_xpath('//*[@id="authors-editor"]//tr[2]//button[contains(@class, "btn-remove")]').click()

        driver.find_element_by_xpath('//*[@id="authors-editor"]//button[@id="modal-button-ok"]').click()

        assert "Alice" in driver.find_element_by_xpath('//*[@class="authors"]//li[1]/span').text
        assert "Bob" not in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text
        assert "Charlie" in driver.find_element_by_xpath('//*[@class="authors"]//li[2]/span').text

    def test_add_affiliations(self):
        driver = self.driver
        self.click_edit_button('affiliations')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="affiliations-editor"]'
                                                  '//button[contains(@class, "btn-add")]'))
        )

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[contains(@class, "btn-add")]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//input[contains(@class, "affil-department")]').send_keys("Bio")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//input[contains(@class, "affil-institution")]').send_keys("University")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//input[contains(@class, "affil-address")]').send_keys("Sesame Street 135a")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//input[contains(@class, "affil-country")]').send_keys("Invenden")

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//select/option[1]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//button[@id="button-assign-affiliation-to-author"]').click()

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]').click()

        assert "Bio" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text

        self.click_edit_button('affiliations')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="affiliations-editor"]'
                                                  '//button[contains(@class, "btn-add")]'))
        )

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[contains(@class, "btn-add")]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[4]'
                                     '//input[contains(@class, "affil-department")]').send_keys("Computational")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[4]'
                                     '//input[contains(@class, "affil-institution")]').send_keys("University")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[4]'
                                     '//input[contains(@class, "affil-address")]').send_keys("Long Street 1-3")
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[4]'
                                     '//input[contains(@class, "affil-country")]').send_keys("Fantidan")

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]//select/option[1]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]'
                                     '//button[@id="button-assign-affiliation-to-author"]').click()

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]//select/option[2]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//tr[6]'
                                     '//button[@id="button-assign-affiliation-to-author"]').click()

        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]').click()

        assert "Computational" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[2]/span').text

    def test_remove_affiliations(self):
        driver = self.driver
        self.click_edit_button('affiliations')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="affiliations-editor"]'
                                                  '//button[contains(@class, "btn-remove")]'))
        )
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//tr[1]//button[contains(@class, "btn-remove")]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]').click()

        assert "Bio" not in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text

        self.click_edit_button('affiliations')
        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="affiliations-editor"]'
                                                  '//button[contains(@class, "button-remove-affiliation-from-author")]'))
        )
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]'
                                     '//button[contains(@class, "button-remove-affiliation-from-author")]').click()
        driver.find_element_by_xpath('//*[@id="affiliations-editor"]//button[@id="modal-button-ok"]').click()

        assert "Computational" in driver.find_element_by_xpath('//*[@class="affiliations"]//li[1]/span').text

    def test_text(self):
        driver = self.driver
        self.click_edit_button('abstract-text')

        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="abstract-text-editor"]'))
        )

        text = driver.find_element_by_xpath('//*[@id="abstract-text-editor"]//textarea[@id="text"]')
        text.send_keys('Test abstract test.')
        driver.find_element(By.XPATH, '//*[@id="abstract-text-editor"]//button[@id="modal-button-ok"]').click()

        assert "Test abstract test." in driver.find_element_by_xpath('//*[@class="abstract-text"]/p').text

    def test_acknowledgements(self):
        driver = self.driver
        self.click_edit_button('acknowledgements')

        WebDriverWait(driver, 30).until(
            EC.visibility_of_element_located((By.XPATH, '//*[@id="acknowledgements-editor"]'))
        )

        acknowledgements = driver.find_element_by_xpath('//*[@id="acknowledgements-editor"]'
                                                        '//textarea[@id="acknowledgements"]')
        acknowledgements.send_keys('Thanks.')
        driver.find_element(By.XPATH, '//*[@id="acknowledgements-editor"]//button[@id="modal-button-ok"]').click()

        assert "Thanks." in driver.find_element_by_xpath('//*[@class="acknowledgements"]/p').text

    def test_add_references(self):
        driver = self.driver
        self.click_edit_button('references')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="references-editor"]//button[contains(@class, "btn-add")]'))
        )

        driver.find_element_by_xpath('//*[@id="references-editor"]//button[contains(@class, "btn-add")]').click()
        driver.find_element_by_xpath('//*[@id="references-editor"]'
                                     '//input[contains(@class, "citation")]').send_keys("John, title.")
        driver.find_element_by_xpath('//*[@id="references-editor"]'
                                     '//input[contains(@class, "link")]').send_keys("www.link.com")
        driver.find_element_by_xpath('//*[@id="references-editor"]'
                                     '//input[contains(@class, "doi")]').send_keys("12345")

        driver.find_element_by_xpath('//*[@id="references-editor"]//button[@id="modal-button-ok"]').click()

        assert driver.find_element_by_xpath('//*[@class="references"]/ol/li')

    def test_remove_references(self):
        driver = self.driver
        self.click_edit_button('references')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="references-editor"]'
                                                  '//button[contains(@class, "btn-remove")]'))
        )

        driver.find_element_by_xpath('//*[@id="references-editor"]//tr[1]'
                                     '//button[contains(@class, "btn-remove")]').click()

        driver.find_element_by_xpath('//*[@id="references-editor"]//button[@id="modal-button-ok"]').click()

    def test_topic(self):
        driver = self.driver
        self.click_edit_button('topic')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="topic-editor"]//div[contains(@class, "radio")]'))
        )

        driver.find_element_by_xpath('//*[@id="topic-editor"]//div[contains(@class, "radio")][2]//input').click()

        driver.find_element_by_xpath('//*[@id="topic-editor"]//button[@id="modal-button-ok"]').click()

        assert "topic two" in driver.find_element_by_xpath('//*[@class="topic"]/p').text

    def test_presentation_type(self):
        driver = self.driver
        self.click_edit_button('poster-or-talk')

        WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, '//*[@id="is-talk-editor"]'))
        )

        driver.find_element_by_xpath('//*[@id="is-talk-editor"]//div[contains(@class, "radio")][2]//input').click()

        driver.find_element_by_xpath('//*[@id="is-talk-editor"]//button[@id="modal-button-ok"]').click()

        assert "Poster" in driver.find_element_by_xpath('//*[@class="poster-or-talk"]//span').text

    def test_submit(self):
        driver = self.driver
        driver.find_element_by_id('button-action').click()
        assert EC.text_to_be_present_in_element(
            (By.XPATH, '//*[@class_name="label-primary"]'), 'Submitted'
        )

    def test_unlock(self):
        driver = self.driver
        driver.find_element_by_id('button-action').click()
        assert EC.text_to_be_present_in_element(
            (By.XPATH, '//*[@class_name="label-primary"]'), 'InPreparation'
        )
