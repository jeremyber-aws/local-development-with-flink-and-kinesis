package myflinkapp.pojo;

public class WorkProfile {
    //color, animal, job position, years worked
    String color;
    String animal;
    String position;
    int yearsWorked;



    public WorkProfile() {
    }

    public WorkProfile(String color, String animal, String position, int yearsWorked) {
        this.color = color;
        this.animal = animal;
        this.position = position;
        this.yearsWorked = yearsWorked;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAnimal() {
        return animal;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getYearsWorked() {
        return yearsWorked;
    }

    public void setYearsWorked(int yearsWorked) {
        this.yearsWorked = yearsWorked;
    }


    @Override
    public String toString() {
        return "WorkProfile{" +
                "color='" + color + '\'' +
                ", animal='" + animal + '\'' +
                ", position='" + position + '\'' +
                ", yearsWorked=" + yearsWorked +
                '}';
    }
}
