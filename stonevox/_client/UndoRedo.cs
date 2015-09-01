using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class UndoRedo : Singleton<UndoRedo>
    {
        public LimitedSizeStack<UndoData> undos;
        public LimitedSizeStack<UndoData> redos;

        public UndoRedo(ClientInput input)
            : base()
        {
            undos = new LimitedSizeStack<UndoData>(100);
            redos = new LimitedSizeStack<UndoData>(100);

            input.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (e.Modifiers == OpenTK.Input.KeyModifiers.Control && e.Key == OpenTK.Input.Key.Z)
                        Undo();
                    if (e.Modifiers == OpenTK.Input.KeyModifiers.Control && e.Key == OpenTK.Input.Key.Y)
                        Redo();
                }
            });
        }

        public void Undo()
        {
            if (undos.Count == 0) return;

            var undo = undos.Pop();

            if (undo.matrix == null) return;

            Singleton<ClientBrush>.INSTANCE.brushes[undo.brush].RemoveVolume(undo.volume, undo.matrix, undo.data);
            undo.matrix.Clean();
            redos.Push(undo);
        }

        public void Redo()
        {
            if (redos.Count == 0) return;

            var redo = redos.Pop();

            if (redo.matrix == null) return;

            Singleton<ClientBrush>.INSTANCE.brushes[redo.brush].AddVolume(redo.volume, redo.matrix, ref redo.color, redo.data);
            redo.matrix.Clean();
            undos.Push(redo);
        }

        public void AddUndo(VoxelBrushType type, QbMatrix matrix, VoxelVolume volume, Colort color, Dictionary<double, VoxelUndoData> data)
        {
            undos.Push(new UndoData(type, matrix, volume, color, data));
        }
    }

    public struct UndoData
    {
        public QbMatrix matrix;
        public Dictionary<double, VoxelUndoData> data;
        public VoxelVolume volume;
        public Colort color;
        public VoxelBrushType brush;

        public UndoData(VoxelBrushType type, QbMatrix matrix, VoxelVolume volume, Colort color, Dictionary<double, VoxelUndoData> data)
        {
            this.brush = type;
            this.matrix = matrix;
            this.volume = volume;
            this.color = color;
            this.data = new Dictionary<double, VoxelUndoData>();

            foreach (var c in data)
            {
                this.data.Add(c.Key, c.Value);
            }
        }
    }

    public struct VoxelUndoData
    {
        public int colorindex;
        public bool changed;
        public byte alphamask;

        public VoxelUndoData(bool changed)
        {
            this.colorindex = 0;
            this.changed = changed;
            this.alphamask = 0;
        }

        public VoxelUndoData(int colorindex, byte alphamask)
        {
            this.colorindex = colorindex;
            this.changed = false;
            this.alphamask = alphamask;
        }
    }

    public class LimitedSizeStack<T> : LinkedList<T>
    {
        private readonly int _maxSize;
        public LimitedSizeStack(int maxSize)
        {
            _maxSize = maxSize;
        }

        public void Push(T item)
        {
            this.AddFirst(item);

            if (this.Count > _maxSize)
                this.RemoveLast();
        }

        public T Pop()
        {
            var item = this.First.Value;
            this.RemoveFirst();
            return item;
        }
    }
}
