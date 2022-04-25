package test;

public class StatLib {
    // simple average
    public static float avg(float[] x){
        float avg = 0;
        for (float v : x) avg += v;
        return avg/x.length;
    }

    // returns the variance of X and Y
    public static float var(float[] x){
        float avg = avg(x);
        float var = 0;
        for (float v : x) var += (v - avg) * (v - avg);
        return  var / x.length;
    }

    // returns the covariance of X and Y
    public static float cov(float[] x, float[] y){
        float avg_x = avg(x);
        float avg_y = avg(y);
        float cov = 0;

        for(int i=0; i < x.length; i++)
            cov += (x[i] - avg_x) * (y[i] - avg_y);

        return cov/x.length;

    }

    // returns the Pearson correlation coefficient of X and Y
    public static float pearson(float[] x, float[] y){
        float cov_XY = cov(x, y);
        float stan_dev_x = (float) Math.sqrt(var(x)); //standard deviation of x
        float stan_dev_y = (float) Math.sqrt(var(y)); //standard deviation of x
        return cov_XY / (stan_dev_x * stan_dev_y);
    }

    // performs a linear regression and returns the line equation
    public static Line linear_reg(Point[] points){
        float avg_x, avg_y , cov , var , a ,b ;
        float[] x = new float[points.length];
        float[] y = new float[points.length];
        for(int i=0; i< points.length; i++)
        {
            x[i] = points[i].x;
            y[i] = points[i].y;
        }

        avg_x = avg(x);
        avg_y = avg(y);
        cov = cov(x , y);
        var = var(x);
        a = cov/var;
        b = avg_y - (a * avg_x);

        Line line;
        line = new Line(a,b);

        return line;
    }

    // returns the deviation between point p and the line equation of the points
    public static float dev(Point p,Point[] points){
        Line line = linear_reg(points);
        return Math.abs(line.f(p.x) - p.y);
    }

    // returns the deviation between point p and the line
    public static float dev(Point p,Line l){
        return Math.abs(l.f(p.x) - p.y);
    }
}
