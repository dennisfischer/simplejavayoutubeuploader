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

var license = "%s";
var monetize = "%s";
var monetizeOverlay = "%s";
var monetizeTrueview = "%s";
var monetizeProduct = "%s";
var release = "%s";
var releaseDate = "%s";
var releaseTime = "%s";
var publish = "%s";
var partner = "%s";


var claimtype = parseInt("%s");
var policytype = parseInt("%s") + 1;
var partnerOverlay = "%s";
var partnerTrueview = "%s";
var partnerInstream = "%s";
var partnerProduct = "%s";

//ASSET VALUE
var assetValue = "%s";

var webTitle = "%s";
var webDescription = "%s";
var webID = "%s";
var webNotes = "%s";

var tvTMSID = "%s";
var tvISAN = "%s";
var tvEIDR = "%s";
var showTitle = "%s";
var episodeTitle = "%s";
var seasonNb = "%s";
var episodeNb = "%s";
var tvID = "%s";
var tvNotes = "%s";

var movieTitle = "%s";
var movieDescription = "%s";
var movieTMSID = "%s";
var movieISAN = "%s";
var movieEIDR = "%s";
var movieID = "%s";
var movieNotes = "%s";


function loaded() {

	if (document.getElementsByClassName("license-input")[0] == null || document.getElementsByClassName('enable-monetization')[0] == null) {
		return;
	}
	else {
		clearInterval(interval);
	}
	if (license == "true") {
		var inputLicense = document.getElementsByClassName("license-input")[0];
		inputLicense.selectedIndex = 1;
		fireEvent(inputLicense, "change");
	}

	var monetizeCheckbox = document.getElementsByClassName('enable-monetization')[0];
	if (monetize == "true") {

		fireEvent(monetizeCheckbox, "click");

		var disclaimer = document.getElementsByClassName('monetization-disclaimer-accept')[0];
		if (disclaimer != null) {
			fireEvent(disclaimer, "click");
		}

		var overlay = document.getElementsByClassName("ads-settings-enable-overlay-ads")[0];

		if (monetizeOverlay == "true" && overlay.checked == false || monetizeOverlay != "true" && overlay.checked == true) {
			fireEvent(overlay, "click");
		}

		var trueview = document.getElementsByClassName("ads-settings-trueview-instream")[0];

		if (monetizeTrueview == "true" && trueview.checked == false || monetizeTrueview != "true" && trueview.checked == true) {
			fireEvent(trueview, "click");
		}

		var product = document.getElementsByClassName("ads-settings-paid-product")[0];

		if (monetizeProduct == "true" && product.checked == false || monetizeProduct != "true" && product.checked == true) {
			fireEvent(product, "click");
		}

	}

	if (release == "true") {
		var inputPrivacy = document.getElementsByName("privacy")[0];
		if (inputPrivacy.options.length == 4) {
			inputPrivacy.selectedIndex = 3;
			fireEvent(inputPrivacy, "change");

			var date = document.getElementsByClassName("publish-date-formatted")[0];
			date.value = releaseDate;

			var time = document.getElementsByClassName("publish-time-formatted")[0];
			selectOptionByValue(time, releaseTime);
			fireEvent(time, "change");
		}
	}

	if (publish == "true") {
		var inputPublish = document.getElementsByClassName("metadata-privacy-input")[0];
		inputPublish.selectedIndex = 0;
		fireEvent(inputPublish, "change");

	}

	if (partner == "true") {

		fireEvent(monetizeCheckbox, "click");
		var type = document.getElementsByName("claim_type")[0];

		if (type != null) {
			type.selectedIndex = claimtype;
			fireEvent(type, "change");

			var policy = document.getElementsByClassName("usage_policy-select")[0];
			policy.selectedIndex = policytype;
			fireEvent(policy, "change");

			if (policy.selectedIndex == 1) {
				var overlayPartner = document.getElementsByClassName("ads-settings-enable-overlay-ads")[0];
				if (partnerOverlay == "true" && overlayPartner.checked == false || partnerOverlay != "true" && overlayPartner.checked == true) {
					fireEvent(overlayPartner, "click");
				}

				var trueviewPartner = document.getElementsByClassName("ads-settings-trueview-instream")[0];
				if (partnerTrueview == "true" && trueviewPartner.checked == false || partnerTrueview != "true" && trueviewPartner.checked == true) {
					fireEvent(trueviewPartner, "click");
				}

				var instreamPartner = document.getElementsByClassName("ads-settings-instream")[0];
				if (partnerInstream == "true" && instreamPartner.checked == false || partnerInstream != "true" && instreamPartner.checked == true) {
					fireEvent(instreamPartner, "click");
				}

				var productPartner = document.getElementsByClassName("ads-settings-paid-product")[0];
				if (partnerProduct == "true" && productPartner.checked == false || partnerProduct != "true" && productPartner.checked == true) {
					fireEvent(productPartner, "click");
				}
			}

			var assets = document.getElementsByName("asset_type");
			for (var i = 0; i < assets.length; i++) {
				if (assets[i].value == assetValue) {
					fireEvent(assets[i], "click");
				}
			}

			if (assetValue == "web") {
				document.getElementsByName("web_title")[0].value = webTitle;
				document.getElementsByName("web_description")[0].value = webDescription;
				document.getElementsByName("web_custom_id")[0].value = webID;
				document.getElementsByName("web_notes")[0].value = webNotes;
			}
			else if (assetValue == "tv") {
				document.getElementsByName("tv_tms_id")[0].value = tvTMSID;
				document.getElementsByName("tv_isan")[0].value = tvISAN;
				document.getElementsByName("tv_eidr")[0].value = tvEIDR;
				document.getElementsByName("show_title")[0].value = showTitle;
				document.getElementsByName("episode_title")[0].value = episodeTitle;
				document.getElementsByName("season_nb")[0].value = seasonNb;
				document.getElementsByName("episode_nb")[0].value = episodeNb;
				document.getElementsByName("tv_custom_id")[0].value = tvID;
				document.getElementsByName("tv_notes")[0].value = tvNotes;
			} else if (assetValue == "movie") {
				document.getElementsByName("movie_title")[0].value = movieTitle;
				document.getElementsByName("movie_description")[0].value = movieDescription;
				document.getElementsByName("movie_tms_id")[0].value = movieTMSID;
				document.getElementsByName("movie_isan")[0].value = movieISAN;
				document.getElementsByName("movie_eidr")[0].value = movieEIDR;
				document.getElementsByName("movie_custom_id")[0].value = movieID;
				document.getElementsByName("movie_notes")[0].value = movieNotes;
			}
		}
	}

	document.getElementsByClassName("save-changes-button")[0].click();

	setTimeout(function () {
		window.location = "https://www.youtube.com/";
	}, 5000);
}

if (typeof document.getElementsByClassName != 'function') {
	document.getElementsByClassName = function () {
		var elms = document.getElementsByTagName('*');
		var ei = new Array();
		for (var i = 0; i < elms.length; i++) {
			if (elms[i].getAttribute('class')) {
				var ecl = elms[i].getAttribute('class').split(' ');
				for (var j = 0; j < ecl.length; j++) {
					if (ecl[j].toLowerCase() == arguments[0].toLowerCase()) {
						ei.push(elms[i]);
					}
				}
			}
			else if (elms[i].className) {
				ecl = elms[i].className.split(' ');
				for (j = 0; j < ecl.length; j++) {
					if (ecl[j].toLowerCase() == arguments[0].toLowerCase()) {
						ei.push(elms[i]);
					}
				}
			}
		}
		return ei;
	};
}

if (typeof fireEvent != 'function') {
	/**
	 * Fire an event handler to the specified node. Event handlers can detect that the event was fired programatically
	 * by testing for a 'synthetic=true' property on the event object
	 * @param {Node} node The node to fire the event handler on.
	 * @param {String} eventName The name of the event without the "on" (e.g., "focus")
	 */
	function fireEvent(node, eventName) {
		// Make sure we use the ownerDocument from the provided node to avoid cross-window problems
		var doc;
		if (node.ownerDocument) {
			doc = node.ownerDocument;
		} else if (node.nodeType == 9) {
			// the node may be the document itself, nodeType 9 = DOCUMENT_NODE
			doc = node;
		} else {
			throw new Error("Invalid node passed to JSUtil.fireEvent: " + node.id);
		}

		var event;
		if (node.dispatchEvent) {
			// Gecko-style approach is much more difficult.
			var eventClass = "";

			// Different events have different event classes.
			// If this switch statement can't map an eventName to an eventClass,
			// the event firing is going to fail.
			switch (eventName) {
				case "click": // Dispatching of 'click' appears to not work correctly in Safari. Use 'mousedown' or 'mouseup' instead.
				case "mousedown":
				case "mouseup":
					eventClass = "MouseEvents";
					break;

				case "focus":
				case "change":
				case "blur":
				case "select":
					eventClass = "HTMLEvents";
					break;

				default:
					throw "JSUtil.fireEvent: Couldn't find an event class for event '" + eventName + "'.";
					break;
			}
			event = doc.createEvent(eventClass);
			var bubbles = eventName != "change";
			event.initEvent(eventName, bubbles, true); // All events created as bubbling and cancelable.

			event.synthetic = true; // allow detection of synthetic events
			node.dispatchEvent(event);
		}
		else if (node.fireEvent) {
			// IE-style
			event = doc.createEventObject();
			event.synthetic = true; // allow detection of synthetic events
			node.fireEvent("on" + eventName, event);
		}
	}
}

function selectOptionByValue(selObj, val) {
	var L = selObj.options.length;
	while (L != 0) {
		if (selObj.options[--L].value == val) {
			selObj.selectedIndex = L;
			L = 0;
		}
	}
}


var interval = setInterval(loaded, 500);