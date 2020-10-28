Frontend Testing With Selenium
=============================

This file contains a description about necessary steps to start testing with selenium as well as some remarks about setting up new tests.

# Required Selenium setup
Note that this setup has been tested and used exclusively with Python 3.6. Using Python 2 is not
encouraged.

- Download the Selenium server (`selenium-server-standalone-[version].jar`) from the [selenium page](https://www.seleniumhq.org/download)
- Information on how to get the webdrivers for Chrome and Firefox ("Geckodriver") can also be found on the page above. Since the location of these drivers has changed in the past, selenium does not link directly. The latest locations for driver downloads at this point where
  - Firefox: [github](https://github.com/mozilla/geckodriver/releases)
  - Chrome: [sites.google.com](https://sites.google.com/a/chromium.org/chromedriver)
- Make sure the server jar and the webdrivers are in a location that is available via the `PATH` environmental variable.
- Install the required python dependencies; `pip install selenium pytest`.
- Open a python shell and run the following code to check if the webdrivers work
  
      from selenium import webdriver
      cdriver = webdriver.Chrome()
      fdriver = webdriver.Firefox()

- If any problems arise at this point, check whether the driver that was downloaded supports the actually installed corresponding browser.

# Preliminary requirements

## Test database setup
- Make sure that `activator test` has been run; all frontend tests depend on the the h2 test database content which is created when the backend tests are executed.
- Make sure GCA-Web is running locally at the default port `9000` using `activator run`.

## Selenium preparation
To allow parallel and cross browser testing and even giving the possibility of testing with different operating systems and on different machines, we next set up selenium grid using the selenium server jar mentioned above.

The hub can be started by opening a terminal, changing to the selenium folder and then typing in the following command (use your current version of the jar):

```
java -jar selenium-server-standalone-3.141.59.jar -role hub
```

Leave the terminal running, open another terminal, navigate to the directory and add a node to the hub:

```
java -jar selenium-server-standalone-3.141.59.jar -role node
```

If you now visit [http://localhost:4444/grid/console](http://localhost:4444/grid/console/) you can see the node 
registered and which browsers are available on each of them. You can set the port, the browser running on the node and various other parameters. It is also possible now to register nodes from different machines, given you know the IP of the hub, by expanding the command above to

```
java -jar selenium-server-standalone-3.141.59.jar -role node -hub http://$IP of hub$:4444/grid/register
```
 
You can also add more nodes via the same command, speeding up the run time of the tests, as they will get distributed over these nodes by `pytest`.

## Running the tests

After the preparation steps above, open another terminal and navigate to the GCA-Web `test/frontend` folder.
The tests can be run by `python -m pytest` or `python -m pytest [testfile]`.

Using the `pytest-xdist` module, parallel testing is possible, decreasing the overall run time of the tests. Please note that `pytest-xdist` is an extension plugin, not part of the `pytest` distribution and needs to be installed separately. Try to keep the number of open browser windows around 4-6, otherwise the usage of memory space will increase times again.

# Potential issues

- Make sure that you browser and corresponding browser driver versions are compatible. This is likely the 
cause behind errors of type

        selenium.common.exceptions.SessionNotCreatedException: Message: 
          Unable to create new service: ChromeDriverService

- If you obtain a connection error in the browser, be sure that no proxy server is set in your browser. 
This might also cause the browser to load slowly.

# Creating new tests

There are some issues you can encounter, when adding tests.
- Using `pytest` and fixtures, the `unittest.TestCase` class does not work.
- For each class, you can add another fixture in the `conftest.py` file.
- Driver set-up and starting at the login page is already contained in the `maximize_login(request)` function.  
- Working with the selenium standalone server, set up new drivers via

    ```python
    driver = webdriver.Remote(
                command_executor="http://" + Cookies.get_host_ip() + ":4444/wd/hub",
                desired_capabilities={'browserName': '$driver_name$', 'javascriptEnabled': True}
            )
    ```

- In case you want to run the tests without the standalone server, replace the remote webdriver. You only need to do this once in the `maximize_login(request)` function.

    ```python
    driver = webdriver.Firefox()
    # or
    driver = webdriver.Chrome()
    ```

- If you want to add cookies, first open up another page, load the cookies and the go to the desired page or reload. Compare with the already used set up functions in the `conftest.py` file. An issue with selenium is that using `localhost` in URLs can cause problems with, e.g. loading cookies. As a workaround, always use the `Cookies.get_host_ip()`.

- With most browser drivers, elements searched for by functions of the type `driver.get_element_by_*()` will be 
found automatically, even if they're outside of the section currently viewed on the screen. In Firefox (Geckodriver) an error will be returned in such cases. It is important for Firefox to always navigate/scroll to the element before examination by using `move_to_element_by_*()`. For often used actions like the `click()`, `send_keys()` or `get_attribute()` functions including this are predefined in the `conftest.py` file.

- Generally tests run stable. In some incidences a test might fail due to local issues, repeating the test might work then. To avoid such problems, it is useful to include the following code snipped after processes that are expected to need some time to execute, e.g. when saving a form or loading a page or modal.

    ```python
    WebDriverWait(driver, 30).until(
        # insert expected condition, e.g.
        EC.presence_of_element_located((By.XPATH, ''))
    )
    ```

# References and links

- For all things python and selenium, it's good to have a look at this page  
https://selenium-python.readthedocs.io/

- For more helpful information about selenium grid, check out the selenium documentation  
https://www.selenium.dev/documentation/en/

- For information on selenium and docker, see [this article on medium.com](https://medium.com/@arnonaxelrod/running-cross-browser-selenium-tests-in-parallel-using-selenium-grid-and-docker-containers-9ee293b86cfd)

- A short tutorial for setting up selenium grid and creating a test class using `unittest.TestCase` instead of fixtures can be found here  
https://gist.github.com/dzitkowskik/0fc641cf59af0dc3de62


