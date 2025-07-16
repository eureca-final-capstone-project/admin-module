package eureca.capstone.project.admin.common.exception.custom;

public class StatusNotFoundException extends RuntimeException{
    public StatusNotFoundException() {super("상태값을 찾을 수 없습니다.");}
}
