// Autogenerated from runtime/channels/appender.i

package ideal.runtime.channels;

import ideal.library.elements.*;
import ideal.library.channels.*;
import ideal.runtime.elements.*;

public class appender<value_type> implements output<value_type> {
  private final list<value_type> the_list;
  private boolean active;
  public appender(final list<value_type> the_list) {
    this.the_list = the_list;
    this.active = true;
  }
  public appender() {
    this(new base_list<value_type>());
  }
  public @Override void write(final value_type element) {
    assert active;
    the_list.append(element);
  }
  public @Override void write_all(final readonly_list<value_type> elements) {
    assert active;
    the_list.append_all(elements);
  }
  public @Override void sync() { }
  public @Override void close() {
    active = false;
  }
  public immutable_list<value_type> elements() {
    return the_list.elements();
  }
  public immutable_list<value_type> extract_elements() {
    final immutable_list<value_type> result = the_list.elements();
    the_list.clear();
    return result;
  }
}
