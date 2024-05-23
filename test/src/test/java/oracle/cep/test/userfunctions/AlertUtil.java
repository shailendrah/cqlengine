package oracle.cep.test.userfunctions;

public class AlertUtil
{

  public static int getDuration(String paramName)
  {
    if(paramName.equals("TaskAcceptedNotBooked"))
      return 1;
    else
      return 2;
  }
}
