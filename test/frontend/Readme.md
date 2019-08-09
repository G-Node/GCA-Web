Frontend Testing With Selenium
=============================

This file contains a description about the steps necessary in order to start testing with selenium 
as well as some remarks about setting up new tests.

# Steps to Set Up Selenium

Before the tests can run, several steps are needed in order for them to work.

First of course, you need to install python-selenium, if you haven't done so already, 
after setting up a virtual environment (not necessarily in this order):

`pip install selenium`

Next, you need to get the drivers for the browsers your interested in, at the moment, 
Google Chrome and Firefox are used. The drivers can eb downloaded from this page:

https://www.seleniumhq.org/download/

under "Third Party Drivers, Bindings, and Plugins". 
Place them on your local machine and make sure the PATH is set right.

To allow parallel and cross browser testing, and even giving the possiblity of testing
with different operating systems and on different machines, we next set up selenium grid.
In order to do this, we need the selenium standalone server, which is already included as a java jar 
in the "selenium" directory. Be sure to check out newer versions from time to time on the same page as above.
A hub is set up  accessible via port 4444 by default, where nodes with different browsers can be added.

The hub can be started by opening a terminal, changing to the selenium folder and then typing in the following command
(replace with you current version of the jar):

```
cd $path$/selenium  
java -jar selenium-server-standalone-3.141.59.jar -role hub
```

Leave the terminal running, open another terminal, navigate to the directory and add a node to the hub:

```
java -jar selenium-server-standalone-3.141.59.jar -role node
```

If you now visit [http://localhost:4444/grid/console](http://localhost:4444/grid/console/) you can see the node 
registered and which browsers are available on each of them.
You can set the port, the browser running on the node and various other parameters. 
It is also possible now to register nodes from different machines, given you know the IP of the hub, by expanding the 
command above to

```
java -jar selenium-server-standalone-3.141.59.jar -role node -hub http://$IP of hub$:4444/grid/register
```
 
Using just your local machine, you can also add more nodes via the same command,
speeding up the run time of the tests, as they will get distributed over these nodes by pytest.
For more helpful information about selenium grid, check out [the docs](https://www.seleniumhq.org/docs/07_selenium_grid.jsp) and this [article on medium.com](https://medium.com/@arnonaxelrod/running-cross-browser-selenium-tests-in-parallel-using-selenium-grid-and-docker-containers-9ee293b86cfd)

After this preparation, open yet another terminal. Before starting the tests, be sure to create a file containing 
session cookies:

```
python load_cookies.py
```

Now we can run the tests via the `pytest` or alternatively the `python -m pytest` command. Without any arguments,
all tests in the directory will be executed.
Specific tests can always be run by adding the file name to this command.
Using the `pytest-xdist` module, parallel testing is possible, decreasing the overall run time of the tests.
Try to keep the number of open browser windows around 4-6, 
otherwise the usage of memory space will increase times again.

##Possible Problem Solutions

- Make absolutely sure that you browser and corresponding browser driver versions are compatible. This is likely the 
cause behind errors of type:
``selenium.common.exceptions.SessionNotCreatedException: Message: Unable to create new service: ChromeDriverService``
- If you obtain a connection error in the browser, be sure that no proxy server is set in your browser. 
This might also cause the browser to load slowly.

# Creating New Tests

There are some issues you can encounter, when adding tests.
Also there are many different ways to set up the frontend tests with selenium, unittest and pytest.

Using `pytest` and fixtures, the unittest.TestCase class does not work.
For each class, you can add another fixture in the conftest.py file.
Driver set-up and starting at the login page is already contained in the `maximize_login(request)` function.  
Working with the selenium standalone server, please set up new drivers via:

```python
driver = webdriver.Remote(
            command_executor="http://" + Cookies.get_host_ip() + ":4444/wd/hub",
            desired_capabilities={'browserName': '$driver_name$', 'javascriptEnabled': True}
        )
```

In case, you want to run the tests without the standalone server, replace the remote webdriver.

```python
driver = webdriver.Firefox()
#or
driver = webdriver.Chrome()
```

You only need to do this once in the `maximize_login(request)` function.

If you want to add cookies, first open up another page, load the cookies and the go to the desired page or reload.
Compare with the already used set up functions in the conftest.py file.
An issue with selenium is that using `localhost` in URLs can cause problems with, e.g. loading cookies.
As a workaround, always use the `Cookies.get_host_ip()`.

For all things python and selenium, it's good to have a look at this page:  
https://selenium-python.readthedocs.io/

A short tutorial for setting up selenium grid and creating a test class using unittest.TestCase instead of fixtures 
can be found here:  
https://gist.github.com/dzitkowskik/0fc641cf59af0dc3de62


