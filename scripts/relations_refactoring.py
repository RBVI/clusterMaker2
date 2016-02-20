import sys
import re

node_name = {}

def make_query(node1, node2, string_prop1, string_prop2, relationship):
    match_query = 'MATCH (A:{0}),(B:{1})\n'.format(node1, node2)
    where_query = 'WHERE A.name = \"{0}\" AND B.name = \"{1}\"\n'.format(string_prop1, string_prop2)
    create_query = 'CREATE (A)-[:{}]->(B);\n'.format(relationship)

    return '{}{}{}'.format(match_query, where_query, create_query)

def publish_dict(infile, outfile):
    with open(infile, 'r') as f, open(outfile, 'w') as out:
        lines = f.readlines()
        name_reg = re.compile(r'`name`:"(.+?)"')
        relation_reg = re.compile(r'create (_\d+).+?`(\w+)`.+?(_\d+)$')
        counter = 0
        first_time = True
        for line in lines:
            counter += 1

            if line.startswith(';'):
                break

            if line.startswith('create _'):

                if first_time:
                    out.write(';\n')
                    out.write('commit\n')
                    first_time = False

                ids_and_relation = relation_reg.findall(line)
                name1 = ids_and_relation[0][0]
                name2 = ids_and_relation[0][2]
                relation = ids_and_relation[0][1]
                out.write(make_query('Gene', 'Gene', node_name[name1], node_name[name2], relation))
            elif line.startswith('create ('):

                if counter % 500 == 0:
                    out.write(';\n')
                    out.write('commit\n')
                    out.write('begin\n')

                identifier = line.split(':')[0][8:] # #yolofuckregex
                name = name_reg.search(line).group().split('"')[1]
                node_name[identifier] = name
                out.write(line)
            else:
                out.write(line)


if __name__ == '__main__':
    if len(sys.argv) == 3:
        publish_dict(sys.argv[1], sys.argv[2])
