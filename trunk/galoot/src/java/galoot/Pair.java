package galoot;

public class Pair<I1, I2>
{
    private I1 first;

    private I2 second;

    public Pair(I1 first, I2 second)
    {
        this.first = first;
        this.second = second;
    }

    public I1 getFirst()
    {
        return first;
    }

    public I2 getSecond()
    {
        return second;
    }

}
