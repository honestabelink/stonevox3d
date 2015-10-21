namespace stonevox
{
    [GUIWidgetName("Emptz")]
    [GUIWidgetDataType(typeof(WidgetData))]
    public class EmptyWidget : Widget
    {
        public EmptyWidget() : base()
        {
            this.ID = GetNextAvailableID();
        }

        public EmptyWidget(int id) : base(id)
        {
        }
    }
}
