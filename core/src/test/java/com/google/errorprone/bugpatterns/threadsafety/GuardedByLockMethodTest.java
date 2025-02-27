/*
 * Copyright 2014 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns.threadsafety;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link com.google.errorprone.annotations.concurrent.LockMethod} and {@link
 * com.google.errorprone.annotations.concurrent.UnlockMethod}
 */
@RunWith(JUnit4.class)
public class GuardedByLockMethodTest {

  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(GuardedByChecker.class, getClass());

  @Test
  public void simple() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class Test {",
            "  final Lock lock = null;",
            "  @GuardedBy(\"lock\")",
            "  int x;",
            "  @LockMethod(\"lock\")",
            "  void lock() {",
            "    lock.lock();",
            "  }",
            "  @UnlockMethod(\"lock\")",
            "  void unlock() {",
            "    lock.unlock();",
            "  }",
            "  void m() {",
            "    lock();",
            "    try {",
            "      x++;",
            "    } finally {",
            "      unlock();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithParameter_succeeds() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class Utils {",
            "  @GuardedBy(\"foo\")",
            "  static void mutateFoo(Lock foo) {}",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithParameterMember_succeeds() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class Foo {",
            "  Lock lock;",
            "}",
            "class Utils {",
            "  @GuardedBy(\"foo.lock\")",
            "  static void mutateFoo(Foo foo) {}",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithParameterInstanceMethod_succeeds() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class IndirectFoo {",
            "  Lock getLock() {",
            "    return null;",
            "  }",
            "}",
            "class Foo {",
            "  IndirectFoo indirectFoo;",
            "}",
            "class Utils {",
            "  @GuardedBy(\"foo.indirectFoo.getLock()\")",
            "  static void mutateFoo(Foo foo) {}",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithIndirectParameterInstanceMethod_succeeds() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class IndirectFoo {",
            "  Lock getLock() {",
            "    return null;",
            "  }",
            "}",
            "class Foo {",
            "  IndirectFoo getIndirectFoo() {",
            "    return new IndirectFoo();",
            "  }",
            "}",
            "class Utils {",
            "  @GuardedBy(\"foo.getIndirectFoo().getLock()\")",
            "  static void mutateFoo(Foo foo) {}",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithIndirectParameterInstanceMember_succeeds() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class IndirectFoo {",
            "  Lock lock;",
            "}",
            "class Foo {",
            "  IndirectFoo getIndirectFoo() {",
            "    return new IndirectFoo();",
            "  }",
            "}",
            "class Utils {",
            "  @GuardedBy(\"foo.getIndirectFoo().lock\")",
            "  static void mutateFoo(Foo foo) {}",
            "}")
        .doTest();
  }

  @Test
  public void guardedBy_staticMethodWithInstanceMethod_fails() {
    compilationHelper
        .addSourceLines(
            "threadsafety/Test.java",
            "package threadsafety;",
            "import javax.annotation.concurrent.GuardedBy;",
            "import com.google.errorprone.annotations.concurrent.LockMethod;",
            "import com.google.errorprone.annotations.concurrent.UnlockMethod;",
            "import java.util.concurrent.locks.Lock;",
            "class Utils {",
            "  @GuardedBy(\"getLock()\")",
            "  // BUG: Diagnostic contains:",
            "  // static member guarded by instance",
            "  static void m() {}",
            "  Lock getLock() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }
}
