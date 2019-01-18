function notCompatibleBrowser() {
    var ie = /(Trident|MSIE)[\ /](.)/.exec(navigator.userAgent);

    if (!ie) {
        return false;
    } else {
        var engine = ie[1].toLowerCase(),
            version = Number(ie[2]);

        if (engine === "trident") {
            return version < 7;
        } else {
            return true;
        }
    }
}

if (notCompatibleBrowser()) {
    var msg = "This page requires a recent browser: please update to Internet " +
              "Explorer 11 or switch to Chrome, Opera or Firefox";

    alert(msg);
    throw msg;
}
