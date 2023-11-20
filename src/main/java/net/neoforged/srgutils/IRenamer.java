/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.srgutils;

import net.neoforged.srgutils.IMappingFile.IClass;
import net.neoforged.srgutils.IMappingFile.IField;
import net.neoforged.srgutils.IMappingFile.IMethod;
import net.neoforged.srgutils.IMappingFile.IPackage;
import net.neoforged.srgutils.IMappingFile.IParameter;

public interface IRenamer {
    default String rename(IPackage value) {
        return value.getMapped();
    }

    default String rename(IClass value) {
        return value.getMapped();
    }

    default String rename(IField value) {
        return value.getMapped();
    }

    default String rename(IMethod value) {
        return value.getMapped();
    }

    default String rename(IParameter value) {
        return value.getMapped();
    }
}
