/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

/**
 * contains XML utility functions, some of which are specific to elementtree
 */

var fs = require('fs');
var path = require('path');
var _ = require('underscore');
var et = require('elementtree');

/* eslint-disable no-useless-escape */
var ROOT = /^\/([^\/]*)/;
var ABSOLUTE = /^\/([^\/]*)\/(.*)/;
/* eslint-enable no-useless-escape */

module.exports = {
    // compare two et.XML nodes, see if they match
    // compares tagName, text, attributes and children (recursively)
    equalNodes: function (one, two) {
        if (one.tag !== two.tag) {
            return false;
        } else if (one.text.trim() !== two.text.trim()) {
            return false;
        } else if (one._children.length !== two._children.length) {
            return false;
        }

        if (!attribMatch(one, two)) return false;

        for (var i = 0; i < one._children.length; i++) {
            if (!module.exports.equalNodes(one._children[i], two._children[i])) {
                return false;
            }
        }

        return true;
    },

    // adds node to doc at selector, creating parent if it doesn't exist
    graftXML: function (doc, nodes, selector, after) {
        var parent = module.exports.resolveParent(doc, selector);
        if (!parent) {
            // Try to create the parent recursively if necessary
            try {
                var parentToCreate = et.XML('<' + path.basename(selector) + '>');
                var parentSelector = path.dirname(selector);

                this.graftXML(doc, [parentToCreate], parentSelector);
            } catch (e) {
                return false;
            }
            parent = module.exports.resolveParent(doc, selector);
            if (!parent) return false;
        }

        nodes.forEach(function (node) {
            // check if child is unique first
            if (uniqueChild(node, parent)) {
                var children = parent.getchildren();
                var insertIdx = after ? findInsertIdx(children, after) : children.length;

                // TODO: replace with parent.insert after the bug in ElementTree is fixed
                parent.getchildren().splice(insertIdx, 0, node);
            }
        });

        return true;
    },

    // adds new attributes to doc at selector
    // Will only merge if attribute has not been modified already or --force is used
    graftXMLMerge: function (doc, nodes, selector, xml) {
        var target = module.exports.resolveParent(doc, selector);
        if (!target) return false;

        // saves the attributes of the original xml before making changes
        xml.oldAttrib = _.extend({}, target.attrib);

        nodes.forEach(function (node) {
            var attributes = node.attrib;
            for (var attribute in attributes) {
                target.attrib[attribute] = node.attrib[attribute];
            }
        });

        return true;
    },

    // overwrite all attributes to doc at selector with new attributes
    // Will only overwrite if attribute has not been modified already or --force is used
    graftXMLOverwrite: function (doc, nodes, selector, xml) {
        var target = module.exports.resolveParent(doc, selector);
        if (!target) return false;

        // saves the attributes of the original xml before making changes
        xml.oldAttrib = _.extend({}, target.attrib);

        // remove old attributes from target
        var targetAttributes = target.attrib;
        for (var targetAttribute in targetAttributes) {
            delete targetAttributes[targetAttribute];
        }

        // add new attributes to target
        nodes.forEach(function (node) {
            var attributes = node.attrib;
            for (var attribute in attributes) {
                target.attrib[attribute] = node.attrib[attribute];
            }
        });

        return true;
    },

    // removes node from doc at selector
    pruneXML: function (doc, nodes, selector) {
        var parent = module.exports.resolveParent(doc, selector);
        if (!parent) return false;

        nodes.forEach(function (node) {
            var matchingKid = null;
            if ((matchingKid = findChild(node, parent)) !== null) {
                // stupid elementtree takes an index argument it doesn't use
                // and does not conform to the python lib
                parent.remove(matchingKid);
            }
        });

        return true;
    },

    // restores attributes from doc at selector
    pruneXMLRestore: function (doc, selector, xml) {
        var target = module.exports.resolveParent(doc, selector);
        if (!target) return false;

        if (xml.oldAttrib) {
            target.attrib = _.extend({}, xml.oldAttrib);
        }

        return true;
    },

    pruneXMLRemove: function (doc, selector, nodes) {
        var target = module.exports.resolveParent(doc, selector);
        if (!target) return false;

        nodes.forEach(function (node) {
            var attributes = node.attrib;
            for (var attribute in attributes) {
                if (target.attrib[attribute]) {
                    delete target.attrib[attribute];
                }
            }
        });

        return true;

    },

    parseElementtreeSync: function (filename) {
        var contents = fs.readFileSync(filename, 'utf-8');
        if (contents) {
            // Windows is the BOM. Skip the Byte Order Mark.
            contents = contents.substring(contents.indexOf('<'));
        }
        return new et.ElementTree(et.XML(contents));
    },

    resolveParent: function (doc, selector) {
        var parent, tagName, subSelector;

        // handle absolute selector (which elementtree doesn't like)
        if (ROOT.test(selector)) {
            tagName = selector.match(ROOT)[1];
            // test for wildcard "any-tag" root selector
            if (tagName === '*' || tagName === doc._root.tag) {
                parent = doc._root;

                // could be an absolute path, but not selecting the root
                if (ABSOLUTE.test(selector)) {
                    subSelector = selector.match(ABSOLUTE)[2];
                    parent = parent.find(subSelector);
                }
            } else {
                return false;
            }
        } else {
            parent = doc.find(selector);
        }
        return parent;
    }
};

function findChild (node, parent) {
    var matchingKids = parent.findall(node.tag);
    var i;
    var j;

    for (i = 0, j = matchingKids.length; i < j; i++) {
        if (module.exports.equalNodes(node, matchingKids[i])) {
            return matchingKids[i];
        }
    }
    return null;
}

function uniqueChild (node, parent) {
    var matchingKids = parent.findall(node.tag);
    var i = 0;

    if (matchingKids.length === 0) {
        return true;
    } else {
        for (i; i < matchingKids.length; i++) {
            if (module.exports.equalNodes(node, matchingKids[i])) {
                return false;
            }
        }
        return true;
    }
}

// Find the index at which to insert an entry. After is a ;-separated priority list
// of tags after which the insertion should be made. E.g. If we need to
// insert an element C, and the rule is that the order of children has to be
// As, Bs, Cs. After will be equal to "C;B;A".
function findInsertIdx (children, after) {
    var childrenTags = children.map(function (child) { return child.tag; });
    var afters = after.split(';');
    var afterIndexes = afters.map(function (current) { return childrenTags.lastIndexOf(current); });
    var foundIndex = _.find(afterIndexes, function (index) { return index !== -1; });

    // add to the beginning if no matching nodes are found
    return typeof foundIndex === 'undefined' ? 0 : foundIndex + 1;
}

var BLACKLIST = ['platform', 'feature', 'plugin', 'engine'];
var SINGLETONS = ['content', 'author', 'name'];
function mergeXml (src, dest, platform, clobber) {
    // Do nothing for blacklisted tags.
    if (BLACKLIST.indexOf(src.tag) !== -1) return;

    // Handle attributes
    Object.getOwnPropertyNames(src.attrib).forEach(function (attribute) {
        if (clobber || !dest.attrib[attribute]) {
            dest.attrib[attribute] = src.attrib[attribute];
        }
    });
    // Handle text
    if (src.text && (clobber || !dest.text)) {
        dest.text = src.text;
    }
    // Handle children
    src.getchildren().forEach(mergeChild);

    // Handle platform
    if (platform) {
        src.findall('platform[@name="' + platform + '"]').forEach(function (platformElement) {
            platformElement.getchildren().forEach(mergeChild);
        });
    }

    // Handle duplicate preference tags (by name attribute)
    removeDuplicatePreferences(dest);

    function mergeChild (srcChild) {
        var srcTag = srcChild.tag;
        var destChild = new et.Element(srcTag);
        var foundChild;
        var query = srcTag + '';
        var shouldMerge = true;

        if (BLACKLIST.indexOf(srcTag) !== -1) return;

        if (SINGLETONS.indexOf(srcTag) !== -1) {
            foundChild = dest.find(query);
            if (foundChild) {
                destChild = foundChild;
                dest.remove(destChild);
            }
        } else {
            // Check for an exact match and if you find one don't add
            var mergeCandidates = dest.findall(query)
                .filter(function (foundChild) {
                    return foundChild && textMatch(srcChild, foundChild) && attribMatch(srcChild, foundChild);
                });

            if (mergeCandidates.length > 0) {
                destChild = mergeCandidates[0];
                dest.remove(destChild);
                shouldMerge = false;
            }
        }

        mergeXml(srcChild, destChild, platform, clobber && shouldMerge);
        dest.append(destChild);
    }

    function removeDuplicatePreferences (xml) {
        // reduce preference tags to a hashtable to remove dupes
        var prefHash = xml.findall('preference[@name][@value]').reduce(function (previousValue, currentValue) {
            previousValue[ currentValue.attrib.name ] = currentValue.attrib.value;
            return previousValue;
        }, {});

        // remove all preferences
        xml.findall('preference[@name][@value]').forEach(function (pref) {
            xml.remove(pref);
        });

        // write new preferences
        Object.keys(prefHash).forEach(function (key) {
            var element = et.SubElement(xml, 'preference');
            element.set('name', key);
            element.set('value', this[key]);
        }, prefHash);
    }
}

// Expose for testing.
module.exports.mergeXml = mergeXml;

function textMatch (elm1, elm2) {
    var text1 = elm1.text ? elm1.text.replace(/\s+/, '') : '';
    var text2 = elm2.text ? elm2.text.replace(/\s+/, '') : '';
    return (text1 === '' || text1 === text2);
}

function attribMatch (one, two) {
    var oneAttribKeys = Object.keys(one.attrib);
    var twoAttribKeys = Object.keys(two.attrib);

    if (oneAttribKeys.length !== twoAttribKeys.length) {
        return false;
    }

    for (var i = 0; i < oneAttribKeys.length; i++) {
        var attribName = oneAttribKeys[i];

        if (one.attrib[attribName] !== two.attrib[attribName]) {
            return false;
        }
    }

    return true;
}
