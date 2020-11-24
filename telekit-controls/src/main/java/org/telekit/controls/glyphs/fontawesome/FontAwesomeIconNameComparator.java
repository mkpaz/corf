/*
 * Copyright (c) 2015 Jens Deters http://www.jensd.de
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.telekit.controls.glyphs.fontawesome;

import java.util.Comparator;

/**
 * @author Jens Deters
 */
public class FontAwesomeIconNameComparator implements Comparator<FontAwesomeIcon> {

    @Override
    public int compare(FontAwesomeIcon o1, FontAwesomeIcon o2) {
        if (o1 != null && o2 != null) {
            return o1.name().compareTo(o2.name());
        }
        return 0;
    }
}
