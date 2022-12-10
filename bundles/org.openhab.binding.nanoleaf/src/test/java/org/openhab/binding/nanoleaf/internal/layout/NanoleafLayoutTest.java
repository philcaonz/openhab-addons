/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.nanoleaf.internal.layout;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.binding.nanoleaf.internal.model.PanelLayout;
import org.openhab.core.library.types.HSBType;

import com.google.gson.Gson;

/**
 * Test for layout
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NanoleafLayoutTest {

    @TempDir
    static @Nullable Path temporaryDirectory;

    @ParameterizedTest
    @ValueSource(strings = { "lasvegas.json", "theduck.json", "squares.json", "wings.json", "spaceinvader.json",
            "lines.json" })
    public void testFile(String fileName) throws Exception {
        Path file = Path.of("src/test/resources/", fileName);
        assertTrue(Files.exists(file), "File should exist: " + file);

        Gson gson = new Gson();
        ControllerInfo controllerInfo = gson.fromJson(Files.readString(file, Charset.defaultCharset()),
                ControllerInfo.class);
        assertNotNull(controllerInfo, "File should contain controller info: " + file);

        PanelLayout panelLayout = controllerInfo.getPanelLayout();
        assertNotNull(panelLayout, "The controller info should contain panel layout");

        LayoutSettings settings = new LayoutSettings(true, true, true, true);
        byte[] result = NanoleafLayout.render(panelLayout, new TestPanelState(), settings);
        assertNotNull(result, "Should be able to render the layout: " + fileName);
        assertTrue(result.length > 0, "Should get content back, but got " + result.length + "bytes");

        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r--r--");
        FileAttribute<Set<PosixFilePermission>> attributes = PosixFilePermissions.asFileAttribute(permissions);
        Path outFile = Files.createTempFile(temporaryDirectory, fileName.replace(".json", ""), ".png", attributes);
        Files.write(outFile, result);

        // For inspecting images on own computer
        // Path permanentOutFile = Files.createFile(Path.of("/tmp", fileName.replace(".json", "") + ".png"),
        // attributes);
        // Files.write(permanentOutFile, result);
    }

    private class TestPanelState extends PanelState {
        private final HSBType testColors[] = { HSBType.fromRGB(160, 120, 40), HSBType.fromRGB(80, 60, 20),
                HSBType.fromRGB(120, 90, 30), HSBType.fromRGB(200, 150, 60) };

        public TestPanelState() {
            super(Collections.emptyList());
        }

        @Override
        public HSBType getHSBForPanel(Integer panelId) {
            return testColors[panelId % testColors.length];
        }
    }
}