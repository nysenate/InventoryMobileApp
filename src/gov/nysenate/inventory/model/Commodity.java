package gov.nysenate.inventory.model;

public class Commodity
{
    private int id;
    private String type = "";
    private String category = "";
    private String code = "";
    private String description = "";

    public Commodity() {}

    public Commodity(int id, String type, String category, String code) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commodity commodity = (Commodity) o;

        if (id != commodity.id) return false;
        if (category != null ? !category.equals(commodity.category) : commodity.category != null) return false;
        if (code != null ? !code.equals(commodity.code) : commodity.code != null) return false;
        if (description != null ? !description.equals(commodity.description) : commodity.description != null)
            return false;
        if (type != null ? !type.equals(commodity.type) : commodity.type != null) return false;

        return true;
    }
}
