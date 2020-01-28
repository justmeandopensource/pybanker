FROM python:3.8
RUN git clone -b dev https://github.com/justmeandopensource/pybanker /pybanker
WORKDIR /pybanker/flask-web
RUN pip install --no-cache-dir -r requirements.cfg
ENTRYPOINT [ "python" ]
CMD [ "pybanker.py" ]
