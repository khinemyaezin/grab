package com.product.infrastructure.repository.category;

public class NodeField {
    private String idFieldName;
    private String nameFieldName;
    private String leftFieldName;
    private String rightFieldName;
    private String depthFieldName;

    public String getIdFieldName() {
        return idFieldName;
    }

    public String getNameFieldName() {
        return nameFieldName;
    }

    public String getLeftFieldName() {
        return leftFieldName;
    }

    public String getRightFieldName() {
        return rightFieldName;
    }

    public String getDepthFieldName() {
        return depthFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    public void setNameFieldName(String nameFieldName) {
        this.nameFieldName = nameFieldName;
    }

    public void setLeftFieldName(String leftFieldName) {
        this.leftFieldName = leftFieldName;
    }

    public void setRightFieldName(String rightFieldName) {
        this.rightFieldName = rightFieldName;
    }

    public void setDepthFieldName(String depthFieldName) {
        this.depthFieldName = depthFieldName;
    }
}
