using OpenTK;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public enum Side
{
    Left,
    Right,
    Top,
    Bottom,
    Front,
    Back
}

public class Voxel
{
    public int colorindex;
    public byte alphamask;
    public bool dirty;

    public VoxelSide left;
    public VoxelSide right;
    public VoxelSide top;
    public VoxelSide bottom;
    public VoxelSide front;
    public VoxelSide back;

    public int x;
    public int y;
    public int z;
    
    public Voxel(int x, int y, int z, byte a, int colorindex)
    {
        this.x = x;
        this.y = y;
        this.z = z;

        this.alphamask = a;
        this.colorindex = colorindex;

        left = new VoxelSide();
        right = new VoxelSide();
        top = new VoxelSide();
        bottom = new VoxelSide();
        front = new VoxelSide();
        back = new VoxelSide();
    }

    public override string ToString()
    {
        return string.Format("voxel : {0}, {1}, {2}", x, y, z);
    }
}
