-- Copyright 2014-2020 The Ideal Authors. All rights reserved.
--
-- Use of this source code is governed by a BSD-style
-- license that can be found in the LICENSE file or at
-- https://developers.google.com/open-source/licenses/bsd

class test_elements {

  testcase test_namespace_id() {
    assert text_library.HTML_NS.short_name == "html";
    assert text_library.HTML_NS.to_string() == "html";
  }

  testcase test_element_id() {
    assert text_library.P.short_name == "p";
    assert text_library.P.get_namespace() == text_library.HTML_NS;
    assert text_library.P.to_string() == "html:p";

    assert text_library.DIV.short_name == "div";
    assert text_library.DIV.get_namespace() == text_library.HTML_NS;
    assert text_library.DIV.to_string() == "html:div";
  }

  testcase test_base_element() {
    text_element element : base_element.new(text_library.P);

    assert element.get_id() == text_library.P;
    assert element.attributes().is_empty;
    assert element.children() is null;
  }

  testcase test_make_element() {
    node0 : base_element.new(text_library.P);
    -- TODO: drop cast
    node1 : "foo" as base_string;

    nodes : base_list[text_node].new(node0, node1);
    element : text_util.make_element(text_library.BODY, nodes);

    assert element is base_element;
    assert element.get_id() == text_library.BODY;
    assert element.attributes().is_empty;

    children : element.children();
    assert children is list_text_node;
    child_nodes : children.nodes();
    assert child_nodes.size == 2;

    child0 : child_nodes.first;
    assert child0 is base_element;
    assert child0.get_id() == text_library.P;
    assert child0.attributes().is_empty;
    assert child0.children() is null;

    child1 : child_nodes[1];
    assert child1 is string;
    assert child1 == "foo";
  }
}
