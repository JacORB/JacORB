package Map;

public class PointManagerImpl 
    extends PointManagerPOA 
{
    private Point[] _points = new Point[256];
    private int _num_points;
    private String _manager_name;
    private boolean _verbose;
 
    public PointManagerImpl(String manager_name, boolean verbose) 
    {
        _manager_name = manager_name;
        _num_points = 0;
        _verbose = verbose;
    }

    public Point create_point (int x, int y, String lab, boolean connectable) 
        throws DuplicatePoint, InvalidPoint 
    {
        if (lab == null) {
            throw new InvalidPoint();
        }

        if (_verbose) 
        {
            String connect_string = (connectable ? "connected" : "disconnected");
            System.out.println("PointManager::creating [" + connect_string + 
                               "] point for [" + lab + ", " + x + ", " + y + "]");
        }

        Point the_p = _find_point(lab);
        if (the_p != null) {
            throw new DuplicatePoint();
        }

        Point[] empty_points = new Point[0];
        if (connectable) {
            the_p = new ConnectedPointImpl(x, y, lab, empty_points);
        }
        else {
            the_p = new PointImpl(x, y, lab);
        }

        _add_point(the_p);

        if (_verbose) {
            System.out.println("PointManager::created [" + the_p + "]");
        }

        return the_p;
    }

    public Point get_point (String lab) 
        throws UnknownPoint, InvalidPoint 
    {
        if (lab == null) {
            throw new InvalidPoint();
        }

        Point the_p = _find_point(lab);

        if (the_p == null) 
        {
            throw new UnknownPoint();
        }
        return the_p;
    }

    public void destroy_point (String lab) 
        throws UnknownPoint, InvalidPoint 
    {
        if (lab == null) {
            throw new InvalidPoint();
        }

        if (_verbose) {
            System.out.println("PointManager::destroy_point for [" + lab + "]");
        }

        if (!_remove_point(lab)) {
            throw new UnknownPoint();
        }
    }

    public Point[] list_points (int scaling_factor) 
    {
        if (_verbose) 
        {
            System.out.println("PointManager::list_points with scaling [" + 
                               scaling_factor + "]");
        }

//          Point[] ret = new Point[_num_points * scaling_factor];
        Point[] ret = new Point[_num_points ];

        for (int i = 0; i < _num_points; i++) 
        {
                ret[i] = _points[i];
//              for (int j = 0; j < scaling_factor; j++) 
//              {
//                  ret[i * scaling_factor + j] = _points[i];
//              }
        }
        return ret;
    }

    public void connect_point (String lab, String[] connectTo) 
        throws UnknownPoint, InvalidPoint 
    {
        if (lab == null) {
            throw new InvalidPoint();
        }

        Point the_p = _find_point(lab);
        if (the_p == null) {
            throw new UnknownPoint();
        }

        if (!(the_p instanceof ConnectedPoint)) {
            throw new InvalidPoint();
        }

        ConnectedPoint the_cp = (ConnectedPoint)the_p;

        for (int i = 0; i < connectTo.length; i++) {
            Point target = _find_point(connectTo[i]);
            if (target == null) {
                throw new UnknownPoint(lab);
            }
            the_cp.add_connection(target);
        }
    }

    public String get_name() {
        return _manager_name;
    }

    private int _find(String label) 
    {

        int i;
        for (i = 0; i < _num_points; i++) 
        {
            if (label.equals(_points[i].label)) 
            {
                break;
            }
        }
        return i;
    }

    private boolean _remove_point(String label) 
    {
        int index = _find(label);
        if (index < _num_points) 
        {
            if (index < _num_points-1) 
            {
                _points[index] = _points[_num_points-1];
            }
            else 
            {
                _points[index] = null;
            }
            _num_points--;
            return true;
        }
        else 
        {
            return false;
        }
    }

    private Point _find_point(String label) 
    {
        int index = _find(label);
        if (index < _num_points) 
        {
            return _points[index];
        }
        return null;
    }

    private void _add_point(Point p) {
        if (_num_points < 256) {
            _points[_num_points++] = p;
        }
    }
}
