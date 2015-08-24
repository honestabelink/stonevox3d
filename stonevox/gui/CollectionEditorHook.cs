using System;
using System.Collections.Generic;
using System.ComponentModel.Design;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    public class CollectionEditorHook : CollectionEditor
    {
        public delegate void CollectionEditorEventHandler(object sender,
                                            FormClosedEventArgs e);

        public static event CollectionEditorEventHandler FormClosed;

        public CollectionEditorHook(Type Type) : base(Type) { }
        protected override CollectionForm CreateCollectionForm()
        {
            CollectionForm collectionForm = base.CreateCollectionForm();
            collectionForm.FormClosed += new FormClosedEventHandler(collection_FormClosed);
            return collectionForm;
        }

        void collection_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (FormClosed != null)
            {
                FormClosed(this, e);
            }
        }
    }
}
