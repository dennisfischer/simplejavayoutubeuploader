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

if (typeof document.getElementsByClassName != 'function') {
    document.getElementsByClassName = function () {
        var elms = document.getElementsByTagName('*');
        var ei = new Array();
        for (var i = 0; i < elms.length; i++) {
            if (elms[i].getAttribute('class')) {
                var ecl = elms[i].getAttribute('class').split(' ');
                for (var j = 0; j < ecl.length; j++) {
                    if (ecl[j].toLowerCase() == arguments[0].toLowerCase()) {
                        ei.push(elms[i])
                    }
                }
            }
            else if (elms[i].className) {
                ecl = elms[i].className.split(' ');
                for (j = 0; j < ecl.length; j++) {
                    if (ecl[j].toLowerCase() == arguments[0].toLowerCase()) {
                        ei.push(elms[i])
                    }
                }
            }
        }
        return ei
    }
}