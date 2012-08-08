/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

var evtClick = document.createEvent("HTMLEvents");
evtClick.initEvent("click", true, true);

var checkbox = document.getElementsByClassName('enable-monetization')[0];
checkbox.dispatchEvent(evtClick);
var disclaimer = document.getElementsByClassName('monetization-disclaimer-accept')[0];
if (disclaimer != null) {
    disclaimer.dispatchEvent(evtClick);
}

var overlay = document.getElementsByClassName("ads-settings-enable-overlay-ads")[0];
var overlayBool = "%s";
if (overlayBool == "true" && overlay.checked == false) {
    overlay.dispatchEvent(evtClick);
} else if (overlayBool != "true" && overlay.checked == true) {
    overlay.dispatchEvent(evtClick);
}

var trueview = document.getElementsByClassName("ads-settings-trueview-instream")[0];
var trueviewBool = "%s";
if (trueviewBool == "true" && trueview.checked == false) {
    trueview.dispatchEvent(evtClick);
} else if (trueviewBool != "true" && trueview.checked == true) {
    trueview.dispatchEvent(evtClick);
}

var product = document.getElementsByClassName("ads-settings-paid-product")[0];
var productBool = "%s";
if (productBool == "true" && product.checked == false) {
    product.dispatchEvent(evtClick);
}
else if (productBool != "true" && product.checked == true) {
    product.dispatchEvent(evtClick);
}