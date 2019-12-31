from flask import current_app as app
import pygal
from pygal.style import LightColorizedStyle
import calendar
from helper_modules.dbHelper import (getInEx, getInExYearly, getEx)
from decimal import Decimal

# Generate line chart for income expense trend since beginning


def inexTrendAll():
    chart = pygal.Line(legend_at_bottom=True, pretty_print=True, x_labels_major_count=30, show_minor_x_labels=False,
                       tooltip_border_radius=10, fill=True, height=400, style=LightColorizedStyle, dots_size=1, x_label_rotation=270)
    income_data = []
    expense_data = []
    labelSeries = []
    inexAllData = getInEx(None, "all")
    if inexAllData:
        for row in inexAllData:
            (year, month) = (str(row[0])[:4], str(row[0])[4:])
            labelSeries.append("%s %s" %
                               (year, calendar.month_abbr[int(month)]))
            income_data.append(Decimal(row[1]))
            expense_data.append(Decimal(row[2]))
        chart.x_labels = labelSeries
        chart.add('Income', income_data)
        chart.add('Expense', expense_data)
    else:
        chart.add('line', [])
    return chart.render_data_uri()

# Generate line chart for income expense yearly trend since beginning


def inexTrendYearlyAll():
    chart = pygal.Line(legend_at_bottom=True, pretty_print=True, x_labels_major_count=30, show_minor_x_labels=False,
                       tooltip_border_radius=10, fill=True, height=400, style=LightColorizedStyle, dots_size=1, x_label_rotation=270)
    income_data = []
    expense_data = []
    savings_data = []
    labelSeries = []
    inexAllYearlyData = getInExYearly()
    if inexAllYearlyData:
        for row in inexAllYearlyData:
            labelSeries.append(row[0])
            income_data.append(round(row[1], 2))
            expense_data.append(round(row[2], 2))
        chart.x_labels = labelSeries
        chart.add('Income', income_data)
        chart.add('Expense', expense_data)
    else:
        chart.add('line', [])
    return chart.render_data_uri()

# Generate dot chart for expense trend since beginning for a user


def exTrendAll():
    chart = pygal.Dot(show_legend=False)
    exAllData = getEx()
    chart.x_labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May',
                      'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'AVG']
    if exAllData:
        yearList = set(x[0] for x in exAllData)
        for year in reversed(sorted(yearList)):
            expenses = [round(x[1], 2) for x in exAllData if x[0] == year]
            if len(expenses) == 12:
                expenses.append(int(sum(expenses)/len(expenses)))
            chart.add('%s' % year, expenses)
    else:
        chart.add('line', [])
    return chart.render_data_uri()
