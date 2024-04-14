/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.srgutils.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import net.neoforged.srgutils.IMappingBuilder;
import org.junit.jupiter.api.Test;

import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IMappingFile.Format;
import net.neoforged.srgutils.INamedMappingFile;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class MappingTest {

    InputStream getStream(String name) {
        return MappingTest.class.getClassLoader().getResourceAsStream(name);
    }

    @Test
    void test() throws IOException {
        IMappingFile pg = IMappingFile.load(getStream("./installer.pg"));
        IMappingFile reverse = pg.reverse();
        for (Format f : Format.values()) {
            pg.write(Paths.get("./build/installer_out." + f.name().toLowerCase()), f, false);
            reverse.write(Paths.get("./build/installer_out_rev." + f.name().toLowerCase()), f, false);
        }
    }

    @Test
    void reverse() throws IOException {
        IMappingFile a = INamedMappingFile.load(getStream("./installer.pg")).getMap("right", "left");
        IMappingFile b = INamedMappingFile.load(getStream("./installer.pg")).getMap("left", "right").reverse();
        a.getClasses().forEach(ca -> {
            IMappingFile.IClass cb = b.getClass(ca.getOriginal());
            assertNotNull(cb, "Could not find class: " + ca);
            ca.getFields().forEach(fa -> {
                IMappingFile.IField fb = cb.getField(fa.getOriginal());
                assertNotNull(fb, "Could not find field: " + fa);
                assertEquals(fa.getMapped(), fb.getMapped(), "Fields did not match: " + fa + "{" + fa.getMapped() + " -> " + fb.getMapped() + "}");
            });
            ca.getMethods().forEach(ma -> {
                IMappingFile.IMethod mb = cb.getMethod(ma.getOriginal(), ma.getDescriptor());
                if (mb == null) {
                    //Assertions.assertNotNull(mb, "Could not find method: " + ma);
                    StringBuilder buf = new StringBuilder();
                    buf.append("Could not find method: " + ma);
                    cb.getMethods().forEach(m -> {
                        buf.append("\n  ").append(m.toString());
                    });
                    throw new IllegalArgumentException(buf.toString());
                }
                assertEquals(ma.getMapped(), mb.getMapped(), "Methods did not match: " + ma + "{" + ma.getMapped() + " -> " + mb.getMapped() + "}");
                assertEquals(ma.getMappedDescriptor(), mb.getMappedDescriptor(), "Method descriptors did not match: " + ma + "{" + ma.getMappedDescriptor() + " -> " + mb.getMappedDescriptor() + "}");
            });
        });
    }

    @Test
    void tinyV2Comments() throws IOException {
        IMappingFile map = INamedMappingFile.load(getStream("./tiny_v2.tiny")).getMap("left", "right");

        IMappingFile.IClass cls = map.getClass("Foo");
        assertNotNull(cls, "Missing class");
        assertEquals("Class Comment", cls.getMetadata().get("comment"));

        IMappingFile.IField fld = cls.getField("foo");
        assertNotNull(fld, "Missing field");
        assertEquals("Field Comment", fld.getMetadata().get("comment"));

        IMappingFile.IMethod mtd = cls.getMethod("foo", "()V");
        assertNotNull(mtd, "Missing method");
        assertEquals("Method Comment", mtd.getMetadata().get("comment"));
        assertNotNull(mtd.getParameters(), "Missing parameter collection");

        IMappingFile.IParameter par = mtd.getParameters().iterator().next();
        assertNotNull(par, "Missing Parameter");
        assertEquals("Param Comment", par.getMetadata().get("comment"));
    }

    @Test
    void tinyV2_packages_meta_are_not_written(@TempDir Path tempDir) throws IOException {
        IMappingBuilder builder = IMappingBuilder.create("left", "right");
        builder.addPackage("test/a", "test/b").meta("comment", "foo");
        builder.addPackage("test/c", "test/d").meta("comment", "bar");
        builder.addClass("test/a/Baz", "test/b/Baz").meta("comment", "baz");

        Path output = tempDir.resolve("output.tiny");
        builder.build().write(output, Format.TINY);
        assertLinesMatch(Arrays.asList(
                "tiny\t2\t0\tleft\tright",
                "c\ttest/a/Baz\ttest/b/Baz",
                "\tc\tbaz"
        ), Files.readAllLines(output));
    }

	@Test
	void tsrg2() throws IOException {
		IMappingFile map = INamedMappingFile.load(getStream("./tsrg2.tsrg")).getMap("a", "b");
		IMappingFile.IClass aaeaa = map.getClass("aae$a$a");
		assertEquals("net/test/src/C_5218_", aaeaa.getMapped());
		assertEquals("deserialize", aaeaa.getMethod(
				"a",
				"(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Laae$a;"
		).getMapped());
	}

	@Test
	void tsrg2ExceptionLineNumber() {
		IOException ioException = assertThrows(IOException.class, () ->
				INamedMappingFile.load(getStream("./tsrg2_invalid.tsrg")));
		assertTrue(ioException.getMessage().startsWith("Invalid TSRG v2 line (#4)"));
	}

	@Test
	void tinyV1() throws IOException {
		IMappingFile map = INamedMappingFile.load(getStream("./tiny_v1.tiny")).getMap("a", "b");
		IMappingFile.IClass aaeaa = map.getClass("a");
		assertEquals("net/test/class_4581", aaeaa.getMapped());
		assertEquals("method_22848", aaeaa.getMethod(
				"a",
				"(FF)Lcom/mojang/datafixers/util/Pair;"
		).getMapped());
	}
}
