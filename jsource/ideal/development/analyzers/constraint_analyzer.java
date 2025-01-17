/*
 * Copyright 2014-2020 The Ideal Authors. All rights reserved.
 *
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file or at
 * https://developers.google.com/open-source/licenses/bsd
 */

package ideal.development.analyzers;

import ideal.library.elements.*;
import ideal.runtime.elements.*;
import javax.annotation.Nullable;
import ideal.development.elements.*;
import ideal.development.actions.*;
import ideal.development.constructs.*;
import ideal.development.names.*;
import ideal.development.types.*;
import ideal.development.notifications.*;

public class constraint_analyzer extends single_pass_analyzer {

  private final analyzable expression;

  public constraint_analyzer(constraint_construct source) {
    super(source);
    expression = make(source.expr);
  }

  @Override
  protected analysis_result do_single_pass_analysis() {
    if (has_errors(expression)) {
      return new error_signal(new base_string("Error in assert"), expression, this);
    }

    analysis_result the_result = expression.analyze();

    // TODO: actually check constraint...
    action the_action = library().void_instance().to_action(this);

    if (the_result instanceof action_plus_constraints) {
      immutable_list<constraint> expression_constraints =
          ((action_plus_constraints) the_result).the_constraints;
      readonly_list<constraint> result_constraints = analyzer_utilities.always_by_type(
          expression_constraints, constraint_type.ON_TRUE);
      return action_plus_constraints.make_result(the_action, result_constraints);
    }

    return the_action;
  }
}
