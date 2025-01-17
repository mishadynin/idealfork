/*
 * Copyright 2014-2020 The Ideal Authors. All rights reserved.
 *
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file or at
 * https://developers.google.com/open-source/licenses/bsd
 */

package ideal.machine.elements;

import ideal.library.elements.*;
import ideal.library.channels.*;
import ideal.runtime.elements.base_string;
import ideal.runtime.elements.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class runtime_util {

  private static final Map<Class, Field[]> cache = new HashMap<Class, Field[]>();

  public static string short_class_name(Object obj) {
    return main_name(obj.getClass().getName());
  }

  private static string main_name(String name) {
    int dot = name.lastIndexOf('.');
    if (dot > 0) {
      return new base_string(name.substring(dot + 1));
    } else {
      return new base_string(name);
    }
  }

  public static string value_identifier(readonly_value the_value) {
    return new base_string(short_class_name(the_value), "@",
        Integer.toHexString(System.identityHashCode(the_value)));
  }

  public static Field[] get_fields(final Class c) {
    Field[] result = cache.get(c);

    if (result == null) {
      ArrayList<Field> fields = new ArrayList<Field>();
      Class current = c;

      do {
        for (Field f : current.getDeclaredFields()) {
          int modifiers = f.getModifiers();

          if ((modifiers & Modifier.STATIC) == 0) {
            f.setAccessible(true);
            fields.add(f);
          }
        }

        current = current.getSuperclass();
      } while (current != null);

      result = new Field[fields.size()];
      fields.toArray(result);
      cache.put(c, result);
    }

    return result;
  }

  public static int compute_hash_code(readonly_value d) {
    if (d instanceof readonly_reference_equality) {
      return System.identityHashCode(d);
    }

    if (d instanceof base_string) {
      return ((base_string) d).s().hashCode();
    }

    if (d instanceof immutable_list) {
      return compute_hash_code_list((immutable_list<readonly_data>) d);
    } else {
      return compute_hash_code_composite(d);
    }
  }

  private static int compute_hash_code_list(immutable_list<readonly_data> the_list) {
    int result = 20;

    for (int i = 0; i < the_list.size(); ++i) {
      result = result * 31 + compute_hash_code(the_list.get(i));
    }

    return result;
  }

  private static int compute_hash_code_composite(readonly_value d) {
    Field[] fields = get_fields(d.getClass());
    int result = 10;

    for (int i = 0; i < fields.length; ++i) {
      Field f = fields[i];
      Object val = null;
      int hash;
      try {
        val = f.get(d);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if (val == null) {
        hash = 42;
      } else if (val instanceof readonly_value) {
        hash = compute_hash_code((readonly_value) val);
      } else {
        //utilities.panic("compute_hash_code() for " + val.getClass());
        hash = val.hashCode();
      }
      result = result * 31 + hash;
    }

    return result;
  }

  public static boolean data_equals(readonly_value d1, Object d2) {
    if (d1 == d2) {
      return true;
    }

    if (d1.getClass() != d2.getClass()) {
      return false;
    }

    if (d1 instanceof readonly_reference_equality) {
      return d1 == d2;
    }

    if (d1 instanceof base_string) {
      return ((base_string) d1).s().equals(((base_string) d2).s());
    }

    if (d1 instanceof immutable_list) {
      @SuppressWarnings("unchecked")
      immutable_list<readonly_data> list1 = (immutable_list<readonly_data>) d1;
      @SuppressWarnings("unchecked")
      immutable_list<readonly_data> list2 = (immutable_list<readonly_data>) d2;

      if (list1.size() != list2.size()) {
        return false;
      }

      for (int i = 0; i < list1.size(); ++i) {
        if (!data_equals(list1.get(i), list2.get(i))) {
          return false;
        }
      }

      return true;
    }

    Field[] fields = get_fields(d1.getClass());

    for (int i = 0; i < fields.length; ++i) {
      Field f = fields[i];
      try {
        Object val1 = f.get(d1);
        Object val2 = f.get(d2);
        if (!values_equal(val1, val2)) {
          return false;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return true;
  }

  public static equivalence_with_hash<readonly_value> default_equivalence =
    new default_equivalence_impl();

  private static class default_equivalence_impl implements equivalence_with_hash<readonly_value> {
    @Override
    public Boolean call(readonly_value first, readonly_value second) {
      return runtime_util.values_equal(first, second);
    }

    public int hash(readonly_value the_value) {
      return runtime_util.compute_hash_code(the_value);
    }
  };

  public static boolean values_equal(Object o1, Object o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1 instanceof readonly_value) {
      return data_equals((readonly_value) o1, o2);
    }
    return o1.equals(o2);
  }

  private static final String NULL_STRING = "null";

  public static string concatenate(Object o1, Object o2) {
    String s1 = (o1 != null) ? o1.toString() : NULL_STRING;
    String s2 = (o2 != null) ? o2.toString() : NULL_STRING;
    return new base_string(s1, s2);
  }

  public static void do_panic(String message) {
    System.err.println("PANIC: " + message);
    print_stack();

    try {
      System.exit(1);
    } catch (Throwable t) {
      // We are running as a servlet, can't exit
      throw new RuntimeException(message);
    }
  }

  private static final int SKIP_STACK_FRAMES = 4;

  private static void print_stack() {
    StackTraceElement[] stack_trace = Thread.currentThread().getStackTrace();
    for (int i = SKIP_STACK_FRAMES; i < stack_trace.length; ++i) {
      StackTraceElement element = stack_trace[i];
      System.err.println("       " + runtime_util.main_name(element.getClassName()) + "." +
          element.getMethodName() + "() in " + element.getFileName() + ":" +
          element.getLineNumber());
    }
    System.err.flush();
  }

  // TODO: optimize this; add HTML entity declarations.
  public static string escape_markup(string the_string) {
    StringBuilder sb = new StringBuilder();
    String unwrapped = utilities.s(the_string);

    for (int i = 0; i < unwrapped.length(); ++i) {
      char c = unwrapped.charAt(i);

      switch (c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
          break;
      }
    }

    return new base_string(sb.toString());
  }

  public static void start_test(String name) {
    System.err.print(name + "... ");
    System.err.flush();
  }

  public static void end_test() {
    System.err.println("ok");
    System.err.flush();
  }
}
