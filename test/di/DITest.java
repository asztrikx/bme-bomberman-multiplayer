package test.di;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import di.DI;

public class DITest {
	public class DummyA {
		public int a;

		public DummyA(int a) {
			this.a = a;
		}
	}

	public class DummyB {
		DummyA dummyA = (DummyA) DI.get(DummyA.class);

		public int b;

		public DummyB(int b) {
			this.b = b;
		}
	}

	@Test
	public void addAndGet() {
		DI.put(new DummyA(4));
		DI.put(new DummyB(7));

		DummyB dummyB = (DummyB) DI.get(DummyB.class);
		assertEquals(dummyB.b, 7);
		DummyA dummyA = (DummyA) DI.get(DummyA.class);
		assertEquals(dummyA.a, 4);
	}
}
