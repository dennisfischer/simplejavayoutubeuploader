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

function selectOptionByValue(selObj, val) {
    var A = selObj.options, L = A.length;
    while (L) {
        if (A[--L].value == val) {
            selObj.selectedIndex = L;
            L = 0;
        }
    }
}

var evtChange = document.createEvent("HTMLEvents");
evtChange.initEvent("change", true, true);

var input = document.getElementsByClassName("metadata-privacy-input")[0];
input.selectedIndex = 3;
input.dispatchEvent(evtChange);

var date = document.getElementsByClassName("publish-date-formatted")[0];
date.value = "%s";
date.dispatchEvent(evtChange);

var time = document.getElementsByClassName("publish-time-formatted")[0];
selectOptionByValue(time, "%d");
time.dispatchEvent(evtChange);