// Autogenerated from runtime/elements/base_hash_dictionary.i

package ideal.runtime.elements;

import ideal.library.elements.*;
import ideal.machine.elements.array;

import javax.annotation.Nullable;

public abstract class base_hash_dictionary<key_type, value_type> implements readonly_dictionary<key_type, value_type> {
  public static class parameters {
    public final static int default_size = 16;
  }
  protected static class hash_cell<key_type, value_type> implements dictionary.entry<key_type, value_type> {
    private final key_type the_key;
    protected final int the_key_hash;
    private value_type the_value;
    protected @Nullable hash_cell<key_type, value_type> next;
    public hash_cell(final key_type the_key, final int the_key_hash, final value_type the_value, final @Nullable hash_cell<key_type, value_type> next) {
      this.the_key = the_key;
      this.the_key_hash = the_key_hash;
      this.the_value = the_value;
      this.next = next;
    }
    public hash_cell(final key_type the_key, final int the_key_hash, final value_type the_value) {
      this(the_key, the_key_hash, the_value, null);
    }
    public @Override key_type key() {
      return the_key;
    }
    public @Override value_type value() {
      return the_value;
    }
    protected void set_value(final value_type new_value) {
      this.the_value = new_value;
    }
  }
  protected static class dictionary_state<key_type, value_type> {
    public boolean writable;
    public array<hash_cell<key_type, value_type>> the_buckets;
    public int size;
    public dictionary_state(final int initial_size) {
      writable = true;
      the_buckets = new array<hash_cell<key_type, value_type>>(initial_size);
      size = 0;
    }
    public dictionary_state() {
      this(parameters.default_size);
    }
    protected void clear() {
      if (size != 0) {
        the_buckets = new array<hash_cell<key_type, value_type>>(parameters.default_size);
        size = 0;
      }
    }
    public void reserve(final int reserve_size) {
      if (the_buckets.size >= reserve_size) {
        return;
      }
      int new_size = the_buckets.size * 2;
      if (new_size < reserve_size) {
        new_size = reserve_size;
      }
      final array<base_hash_dictionary.hash_cell<key_type, value_type>> old_buckets = the_buckets;
      the_buckets = new array<hash_cell<key_type, value_type>>(new_size);
      for (int i = 0; i < old_buckets.size; i += 1) {
        @Nullable base_hash_dictionary.hash_cell<key_type, value_type> bucket = old_buckets.at(i).get();
        while (bucket != null) {
          final @Nullable base_hash_dictionary.hash_cell<key_type, value_type> old_next = bucket.next;
          final int new_index = bucket_index(bucket.the_key_hash);
          bucket.next = the_buckets.at(new_index).get();
          the_buckets.set(new_index, bucket);
          bucket = old_next;
        }
      }
      old_buckets.scrub(0, old_buckets.size);
    }
    protected int bucket_index(final int hash) {
      final int bucket_size = the_buckets.size;
      final int index = ((hash % bucket_size) + bucket_size) % bucket_size;
      assert index >= 0;
      return index;
    }
    protected dictionary_state<key_type, value_type> copy() {
      final base_hash_dictionary.dictionary_state<key_type, value_type> result = new dictionary_state<key_type, value_type>(the_buckets.size);
      for (int i = 0; i < the_buckets.size; i += 1) {
        @Nullable base_hash_dictionary.hash_cell<key_type, value_type> bucket = the_buckets.at(i).get();
        while (bucket != null) {
          final base_hash_dictionary.hash_cell<key_type, value_type> new_cell = new hash_cell<key_type, value_type>(bucket.key(), bucket.the_key_hash, bucket.value(), result.the_buckets.at(i).get());
          result.the_buckets.set(i, new_cell);
          bucket = bucket.next;
        }
      }
      result.size = this.size;
      return result;
    }
  }
  protected final equivalence_with_hash<key_type> equivalence;
  protected dictionary_state<key_type, value_type> state;
  protected base_hash_dictionary(final equivalence_with_hash<key_type> equivalence) {
    this.equivalence = equivalence;
    this.state = new dictionary_state<key_type, value_type>();
  }
  protected base_hash_dictionary(final equivalence_with_hash<key_type> equivalence, final dictionary_state<key_type, value_type> state) {
    this.equivalence = equivalence;
    this.state = state;
  }
  public @Override int size() {
    return state.size;
  }
  public @Override boolean is_empty() {
    return state.size == 0;
  }
  public @Override immutable_list<dictionary.entry<key_type, value_type>> elements() {
    if (is_empty()) {
      return new empty<dictionary.entry<key_type, value_type>>();
    }
    final base_list<dictionary.entry<key_type, value_type>> result = new base_list<dictionary.entry<key_type, value_type>>();
    for (int i = 0; i < state.the_buckets.size; i += 1) {
      for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = state.the_buckets.at(i).get(); entry != null; entry = entry.next) {
        result.append(new base_dictionary_entry<key_type, value_type>(entry));
      }
    }
    return result.frozen_copy();
  }
  public @Override immutable_dictionary<key_type, value_type> frozen_copy() {
    return new immutable_hash_dictionary<key_type, value_type>(equivalence, state);
  }
  private @Nullable hash_cell<key_type, value_type> bucket(final int hash) {
    return state.the_buckets.at(state.bucket_index(hash)).get();
  }
  public @Override @Nullable value_type get(final key_type key) {
    assert key != null;
    final int hash = equivalence.hash(key);
    for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = bucket(hash); entry != null; entry = entry.next) {
      if (hash == entry.the_key_hash && equivalence.call(key, entry.key())) {
        return entry.value();
      }
    }
    return null;
  }
  public @Override boolean contains_key(final key_type key) {
    assert key != null;
    final int hash = equivalence.hash(key);
    for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = bucket(hash); entry != null; entry = entry.next) {
      if (hash == entry.the_key_hash && equivalence.call(key, entry.key())) {
        return true;
      }
    }
    return false;
  }
  public @Override immutable_set<key_type> keys() {
    if (is_empty()) { }
    final set<key_type> result = new hash_set<key_type>(equivalence);
    for (int i = 0; i < state.the_buckets.size; i += 1) {
      for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = state.the_buckets.at(i).get(); entry != null; entry = entry.next) {
        result.add(entry.key());
      }
    }
    return result.frozen_copy();
  }
  public @Override readonly_collection<value_type> values() {
    final list<value_type> result = new base_list<value_type>();
    for (int i = 0; i < state.the_buckets.size; i += 1) {
      for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = state.the_buckets.at(i).get(); entry != null; entry = entry.next) {
        result.append(entry.value());
      }
    }
    return result.frozen_copy();
  }
  private string debug_display() {
    string s = new base_string("");
    for (int i = 0; i < state.the_buckets.size; i += 1) {
      s = ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(s, new base_string("b")), i), new base_string(": "));
      for (@Nullable base_hash_dictionary.hash_cell<key_type, value_type> entry = state.the_buckets.at(i).get(); entry != null; entry = entry.next) {
        s = ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(ideal.machine.elements.runtime_util.concatenate(s, new base_string("<")), ((string) entry.key())), new base_string(":")), ((string) entry.value())), new base_string(">"));
      }
      s = ideal.machine.elements.runtime_util.concatenate(s, new base_string("\n"));
    }
    return s;
  }
}
