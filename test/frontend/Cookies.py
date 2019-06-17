import json
import socket


def save_cookies(driver, location):

    json.dump(driver.get_cookies()[0], open(location, "w"))


def load_cookies(driver, url=None, location="cookies.json"):

    cookies = json.load(open(location, "r"))
    driver.delete_all_cookies()
    driver.get("http://" + get_host_ip() + ":9000/login" if url is None else url)
    driver.add_cookie(cookies)


def delete_cookies(driver, domains=None):

    if domains is not None:
        cookies = driver.get_cookies()
        original_len = len(cookies)
        for cookie in cookies:
            if str(cookie["domain"]) in domains:
                cookies.remove(cookie)
        if len(cookies) < original_len:
            driver.delete_all_cookies()
            for cookie in cookies:
                driver.add_cookie(cookie)
    else:
        driver.delete_all_cookies()


def set_cookies(driver, user, password):

    cookies_location = "cookies.json"
    driver.get("http://" + get_host_ip() + ":9000/login")
    driver.maximize_window()
    driver.find_element_by_id("identifier").send_keys(user)
    driver.find_element_by_id("password").send_keys(password)
    driver.find_element_by_id("submit").click()
    save_cookies(driver, cookies_location)


def get_host_ip():
    try:
        host_name = socket.gethostname()
        host_ip = socket.gethostbyname(host_name)
        return host_ip
    except:
        print("Unable to get Hostname and IP")
