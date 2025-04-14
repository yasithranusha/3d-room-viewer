package com.modelviewer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.modelviewer.model.Face;
import com.modelviewer.model.Material;
import com.modelviewer.model.Model3D;

public class ModelLoader {
    public Model3D loadObjFile(String filePath) throws IOException {
        Model3D model = new Model3D(new File(filePath).getName());

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentMaterial = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                switch (parts[0]) {
                    case "v": // Vertex
                        model.getVertices().add(Float.parseFloat(parts[1]));
                        model.getVertices().add(Float.parseFloat(parts[2]));
                        model.getVertices().add(Float.parseFloat(parts[3]));
                        model.updateBounds(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3]));
                        break;

                    case "vn": // Normal
                        model.getNormals().add(Float.parseFloat(parts[1]));
                        model.getNormals().add(Float.parseFloat(parts[2]));
                        model.getNormals().add(Float.parseFloat(parts[3]));
                        break;

                    case "vt": // Texture coordinate
                        model.getTextureCoords().add(Float.parseFloat(parts[1]));
                        model.getTextureCoords().add(parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0f);
                        break;

                    case "f": // Face
                        int[] vertexIndices = new int[parts.length - 1];
                        int[] texCoordIndices = new int[parts.length - 1];
                        int[] normalIndices = new int[parts.length - 1];

                        for (int i = 1; i < parts.length; i++) {
                            String[] indices = parts[i].split("/");
                            vertexIndices[i - 1] = Integer.parseInt(indices[0]) - 1;

                            if (indices.length > 1 && !indices[1].isEmpty()) {
                                texCoordIndices[i - 1] = Integer.parseInt(indices[1]) - 1;
                            }

                            if (indices.length > 2) {
                                normalIndices[i - 1] = Integer.parseInt(indices[2]) - 1;
                            }
                        }

                        model.getFaces().add(new Face(vertexIndices, texCoordIndices, normalIndices, currentMaterial));
                        break;

                    case "mtllib": // Material library
                        if (parts.length > 1) {
                            String mtlPath = new File(filePath).getParent() + File.separator + parts[1];
                            loadMtlFile(mtlPath, model);
                        }
                        break;

                    case "usemtl": // Use material
                        if (parts.length > 1) {
                            currentMaterial = parts[1];
                        }
                        break;
                }
            }
        }

        return model;
    }

    private void loadMtlFile(String filePath, Model3D model) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Material currentMaterial = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                switch (parts[0]) {
                    case "newmtl":
                        if (parts.length > 1) {
                            currentMaterial = new Material(parts[1]);
                            model.getMaterials().put(parts[1], currentMaterial);
                        }
                        break;

                    case "Ka": // Ambient color
                        if (currentMaterial != null && parts.length > 3) {
                            currentMaterial.setAmbient(
                                    Float.parseFloat(parts[1]),
                                    Float.parseFloat(parts[2]),
                                    Float.parseFloat(parts[3]),
                                    1.0f);
                        }
                        break;

                    case "Kd": // Diffuse color
                        if (currentMaterial != null && parts.length > 3) {
                            currentMaterial.setDiffuse(
                                    Float.parseFloat(parts[1]),
                                    Float.parseFloat(parts[2]),
                                    Float.parseFloat(parts[3]),
                                    1.0f);
                        }
                        break;

                    case "Ks": // Specular color
                        if (currentMaterial != null && parts.length > 3) {
                            currentMaterial.setSpecular(
                                    Float.parseFloat(parts[1]),
                                    Float.parseFloat(parts[2]),
                                    Float.parseFloat(parts[3]),
                                    1.0f);
                        }
                        break;

                    case "Ns": // Shininess
                        if (currentMaterial != null && parts.length > 1) {
                            currentMaterial.setShininess(Float.parseFloat(parts[1]));
                        }
                        break;
                }
            }
        }
    }
}