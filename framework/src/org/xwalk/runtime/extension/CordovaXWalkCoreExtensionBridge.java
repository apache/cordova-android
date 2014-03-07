// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension;

import org.xwalk.runtime.extension.XWalkExtension;

/**
 * This class is a public wrapper for XWalkCoreExtensionBridge.
 */
public class CordovaXWalkCoreExtensionBridge extends XWalkCoreExtensionBridge {

    public CordovaXWalkCoreExtensionBridge(XWalkExtension extension) {
        super(extension);
    }
}
