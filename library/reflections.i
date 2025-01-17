-- Copyright 2014-2020 The Ideal Authors. All rights reserved.
--
-- Use of this source code is governed by a BSD-style
-- license that can be found in the LICENSE file or at
-- https://developers.google.com/open-source/licenses/bsd

package reflections {
  implicit import ideal.library.elements;

  interface type_id {
    extends identifier;

    identifier short_name;
  }

  interface entity_wrapper {
    extends stringable;

    type_id type_bound deeply_immutable;
  }

  interface value_wrapper[any value value_type] {
    extends entity_wrapper;

    value_type unwrap() pure;
  }

  interface reference_wrapper[any value value_type] {
    extends entity_wrapper;

    type_id value_type_bound deeply_immutable;

    value_wrapper[value_type] get() pure;
    void init(value_wrapper[value_type] the_value) writeonly;
    void set(value_wrapper[value_type] the_value) writeonly;
  }

  interface variable_id {
    extends identifier;

    identifier short_name;
    type_id value_type;
    type_id reference_type;
  }
}
