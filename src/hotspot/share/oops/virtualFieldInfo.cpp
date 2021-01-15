/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#include "logging/log.hpp"
#include "oops/instanceKlass.hpp"
#include "oops/symbol.hpp"
#include "oops/virtualFieldInfo.hpp"
#include "runtime/signature.hpp"
#include "memory/resourceArea.hpp"

  // Klass* _holder;
  // int _local_index;
  // int _offset;
  // BasicType _type;
  // Klass* _type_klass;

void VirtualFieldInfo::print_value_on(outputStream* st) const {
  st->print_cr("  holder: %s", holder() == NULL ?  "NULL" : holder()->name()->as_C_string());
  st->print_cr("  local index: %d", local_index());
  st->print_cr("  offset: %d", offset());
  BasicType bt = Signature::basic_type(InstanceKlass::cast(holder())->field_signature(local_index()));
  st->print_cr("  type: %s", type2name(bt));
  if (is_reference_type(bt)) {
    st->print_cr("  type_klass: %s", type_klass() == NULL ? "NULL" : type_klass()->name()->as_C_string());
  }
  st->print_cr("  ------------------");
}

void VirtualFieldInfo::print_all(Array<VirtualFieldInfo>* arr, outputStream* st) {
  for (int i = 0; i < arr->length(); i++) {
    st->print_cr("Virtual field [%d]", i);
    arr->at(i).print_value_on(st);
  }
}

void VirtualFieldInfo::metaspace_pointers_do(MetaspaceClosure* it) {
  if (log_is_enabled(Trace, cds)) {
    ResourceMark rm;
    log_trace(cds)("Iter(VirtualFieldInfo): %p [offset=%d]", this, offset());
  }
  it->push(&_holder);
  it->push(&_type_klass);
}